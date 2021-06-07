/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.cloudformation.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.error.AnalysisError;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.iac.common.checks.api.IacCheck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class CloudformationSensorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private static final FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
  private static final NoSonarFilter noSonarFilter = mock(NoSonarFilter.class);

  @TempDir
  File baseDir;
  SensorContextTester context;

  @BeforeEach
  public void setup() {
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
    context = SensorContextTester.create(baseDir);
  }

  @Test
  void should_return_terraform_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Cloudformation Sensor");
    assertThat(descriptor.languages()).containsOnly("cloudformation");
  }

  @Test
  void empty_file_should_raise_no_issue() {
    analyse(sensor("S2260"), inputFile("empty.json", ""));
    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
  }

  @Test
  void parsing_error_should_raise_an_issue_if_check_rule_is_activated() {
    analyse(sensor("S2260"), inputFile("parserError.json", "a {"));

    assertThat(context.allIssues()).as("One issue must be raised").hasSize(1);

    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo("S2260");

//    TODO: Test specific location of parsing error
//    TextRange range = issue.primaryLocation().textRange();
//    assertThat(range).isNotNull();
//    assertThat(range.start().line()).isEqualTo(1);
//    assertThat(range.start().lineOffset()).isEqualTo(0);
//    assertThat(range.end().line()).isEqualTo(1);
//    assertThat(range.end().lineOffset()).isEqualTo(3);
  }

  @Test
  void parsing_error_should_raise_issue_in_sensor_context() {
    analyse(inputFile("parserError.tf", "a {"));
    assertThat(context.allAnalysisErrors()).hasSize(1);
  }

  @Test
  void parsing_error_should_raise_no_issue_if_check_rule_is_not_activated() {
    analyse(inputFile("parserError.tf", "a {"));
    assertThat(context.allIssues()).as("One issue must be raised").isEmpty();
  }

  @Test
  void analysis_error_should_raise_issue_in_sensor_context() throws IOException {
    InputFile inputFile = inputFile("fakeFile.tf", "");
    InputFile spyInputFile = spy(inputFile);
    when(spyInputFile.contents()).thenThrow(IOException.class);
    analyse(spyInputFile);

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(spyInputFile);
    assertThat(analysisError.message()).isEqualTo("Unable to parse file: fakeFile.tf");
    assertThat(analysisError.location()).isNull();

    assertThat(logTester.logs()).contains(String.format("Unable to parse file: %s. ", inputFile.uri()));
  }

  // TODO test valid file for no parsing error

  @Test
  void test_cancellation() {
    context.setCancelled(true);
    analyse(sensor("S2260"), inputFile("parserError.tf", "a {"));
    Collection<Issue> issues = context.allIssues();
    assertThat(issues).isEmpty();
  }

  private void analyse(InputFile... inputFiles) {
    for (InputFile inputFile : inputFiles) {
      context.fileSystem().add(inputFile);
    }
    sensor().execute(context);
  }

  private void analyse(CloudformationSensor sensor, InputFile... inputFiles) {
    for (InputFile inputFile : inputFiles) {
      context.fileSystem().add(inputFile);
    }
    sensor.execute(context);
  }

  private InputFile inputFile(String relativePath, String content) {
    return new TestInputFileBuilder("moduleKey", relativePath)
      .setModuleBaseDir(baseDir.toPath())
      .setType(InputFile.Type.MAIN)
      .setLanguage(CloudformationExtension.LANGUAGE_KEY)
      .setCharset(StandardCharsets.UTF_8)
      .setContents(content)
      .build();
  }

  private CloudformationSensor sensor(String... rules) {
    CheckFactory checkFactory = checkFactory(rules);
    return new CloudformationSensor(fileLinesContextFactory, checkFactory, noSonarFilter, new CloudformationLanguage(new MapSettings().asConfig())) {
      @Override
      protected Checks<IacCheck> checks() {
        Checks<IacCheck> checks = checkFactory.create(CloudformationExtension.REPOSITORY_KEY);
        return checks;
      }
    };
  }

  protected CheckFactory checkFactory(String... ruleKeys) {
    ActiveRulesBuilder builder = new ActiveRulesBuilder();
    for (String ruleKey : ruleKeys) {
      NewActiveRule newRule = new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of(CloudformationExtension.REPOSITORY_KEY, ruleKey))
        .setName(ruleKey)
        .build();
      builder.addRule(newRule);
    }
    context.setActiveRules(builder.build());
    return new CheckFactory(context.activeRules());
  }
}
