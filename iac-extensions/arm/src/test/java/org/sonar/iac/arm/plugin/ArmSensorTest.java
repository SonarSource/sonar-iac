/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.plugin;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.arm.visitors.ArmHighlightingVisitor;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ArmSensorTest extends ExtensionSensorTest {

  @Test
  void should_return_arm_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC AzureResourceManager Sensor");
    assertThat(descriptor.languages()).containsExactly("json", "azureresourcemanager");
  }

  /**
   * When trying to identify if an input file is an ARM file, we look for a specific identifier in the file.
   * There is a limit (8 Kb) on the file reading to not spend an extensive amount of time.
   * This test checks 2 files with valid identifiers. The identifiers are at the end of each file.
   */
  @Test
  void shouldFastCheckFilePredicate() {
    InputFile largeFileWithIdentifier = IacTestUtils.inputFile("large_file_with_identifier.json", "json");
    InputFile mediumFileWithIdentifier = IacTestUtils.inputFile("medium_file_with_identifier.json", "json");

    MapSettings settings = new MapSettings();
    settings.setProperty(ArmSettings.FILE_IDENTIFIER_KEY, ArmSettings.FILE_IDENTIFIER_DEFAULT_VALUE);
    context.setSettings(settings);

    FilePredicate filePredicate = sensor().customFilePredicate(context);
    assertThat(filePredicate.apply(largeFileWithIdentifier)).isFalse();
    assertThat(filePredicate.apply(mediumFileWithIdentifier)).isTrue();
  }

  @Test
  void shouldAllowBicepFiles() {
    InputFile mainBicepInputFile = TestInputFileBuilder.create("moduleKey", "main")
      .setLanguage("azureresourcemanager")
      .setType(InputFile.Type.MAIN)
      .build();

    InputFile testBicepInputFile = TestInputFileBuilder.create("moduleKey", "test")
      .setLanguage("azureresourcemanager")
      .setType(InputFile.Type.TEST)
      .build();

    FilePredicate filePredicate = sensor().mainFilePredicate(context);
    assertThat(filePredicate.apply(mainBicepInputFile)).isTrue();
    assertThat(filePredicate.apply(testBicepInputFile)).isFalse();
  }

  @Override
  protected String getActivationSettingKey() {
    return ArmSettings.ACTIVATION_KEY;
  }

  @Override
  protected ArmSensor sensor(CheckFactory checkFactory) {
    return new ArmSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, new ArmLanguage());
  }

  private ArmSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  @Override
  protected String repositoryKey() {
    return ArmExtension.REPOSITORY_KEY;
  }

  @Override
  protected String fileLanguageKey() {
    return "json";
  }

  @Override
  protected InputFile emptyFile() {
    return inputFile("empty.json", "");
  }

  @Override
  protected InputFile fileWithParsingError() {
    return inputFile("error.json", "\"a'");
  }

  @Override
  protected InputFile validFile() {
    return inputFile("object.json", "{\"key\":\"value\"}");
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(2);
    String message1 = "while scanning a quoted scalar\n" +
      " in reader, line 1, column 1:\n" +
      "    \"a'\n" +
      "    ^\n" +
      "found unexpected end of stream\n" +
      " in reader, line 1, column 4:\n" +
      "    \"a'\n" +
      "       ^\n";
    String message2 = "org.sonar.iac.common.extension.ParseException: Cannot parse 'error.json:1:1'" +
      System.lineSeparator() +
      "\tat org.sonar.iac.common";
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0)).isEqualTo(message1);
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(1)).startsWith(message2);
    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(2);
  }

  @Test
  void shouldIncludeAllVisitorsInRegularContext() {
    List<TreeVisitor<InputFileContext>> visitors = sensor().visitors(context, null);
    assertThat(visitors)
      .hasOnlyElementsOfTypes(MetricsVisitor.class, ChecksVisitor.class, ArmHighlightingVisitor.class)
      .hasSize(3);
  }

  @Test
  void shouldNotIncludeSomeVisitorsInSonarLintContext() {
    List<TreeVisitor<InputFileContext>> visitors = sensor().visitors(sonarLintContext, null);
    assertThat(visitors)
      .doesNotHaveAnyElementsOfTypes(SyntaxHighlightingVisitor.class, MetricsVisitor.class)
      .hasExactlyElementsOfTypes(ChecksVisitor.class);
  }
}
