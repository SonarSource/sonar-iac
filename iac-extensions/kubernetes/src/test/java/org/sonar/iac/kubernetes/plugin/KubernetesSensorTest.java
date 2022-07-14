/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.kubernetes.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.common.testing.AbstractSensorTest;

import static org.assertj.core.api.Assertions.assertThat;

class KubernetesSensorTest extends AbstractSensorTest {

  private static final String K8_IDENTIFIERS = "apiVersion: ~\nkind: ~\nmetadata: ~\nspec: ~\n";
  private static final String PARSING_ERROR_KEY = "S2260";

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void yaml_file_with_identifiers_should_be_parsed() {
    analyse(sensor(), inputFile(K8_IDENTIFIERS));
    assertOneSourceFileIsParsed();
  }

  @Test
  void yaml_file_without_identifiers_should_not_be_parsed() {
    analyse(sensor(), inputFile( ""));
    asserNotSourceFileIsParsed();
  }

  @Test
  void yaml_file_with_incomplete_identifiers_should_not_be_parsed() {
    analyse(sensor(), inputFile("apiVersion: ~\nkind: ~\nmetadata: ~\n"));
    asserNotSourceFileIsParsed();
  }

  @Test
  void yaml_files_within_single_stream_should_be_parsed() {
    analyse(sensor(), inputFile(K8_IDENTIFIERS + "---\n" + K8_IDENTIFIERS));
    assertOneSourceFileIsParsed();
  }

  @Test
  void yaml_files_with_incomplete_identifiers_within_single_stream_should_not_be_parsed() {
    analyse(sensor(), inputFile("apiVersion: ~\nkind: ~\nmetadata: ~\n---\n" + K8_IDENTIFIERS));
    asserNotSourceFileIsParsed();
  }

  @Test
  void yaml_file_with_invalid_syntax_should_not_raise_parsing_if_rule_is_deactivated() {
    analyse(sensor(checkFactory()), inputFileWithIdentifiers("a: b: c"));

    assertThat(context.allAnalysisErrors()).hasSize(1);
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void yaml_file_with_invalid_syntax_should_raise_parsing() {
    analyse(sensor(checkFactory(PARSING_ERROR_KEY)), inputFileWithIdentifiers("a: b: c"));

    assertThat(context.allAnalysisErrors()).hasSize(1);
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo(PARSING_ERROR_KEY);
  }

  @Test
  void yaml_file_with_invalid_syntax_should_not_raise_issue_when_sensor_deactivated() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), false);
    context.setSettings(settings);

    analyse(sensor(checkFactory(PARSING_ERROR_KEY)), inputFileWithIdentifiers("\"a'"));
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void yaml_file_with_recursive_anchor_reference_should_raise_parsing_issue() {
    analyse(sensor(checkFactory(PARSING_ERROR_KEY)), inputFileWithIdentifiers("foo: &fooanchor\n" +
      " bar: *fooanchor"));

    assertThat(context.allAnalysisErrors()).hasSize(1);
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo(PARSING_ERROR_KEY);
  }

  @Test
  void should_return_kubernetes_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Kubernetes Sensor");
    assertThat(descriptor.languages()).containsExactly("json", "yaml");
  }

  private void asserNotSourceFileIsParsed() {
    assertThat(logTester.logs(LoggerLevel.INFO)).contains("0 source files to be analyzed");
  }

  private void assertOneSourceFileIsParsed() {
    assertThat(logTester.logs(LoggerLevel.INFO)).contains("1 source file to be analyzed");
  }

  protected InputFile inputFile(String content) {
    return super.inputFile("k8.yaml", content);
  }

  protected InputFile inputFileWithIdentifiers(String content) {
    return super.inputFile("k8.yaml", K8_IDENTIFIERS + content);
  }

  @Override
  protected String getActivationSettingKey() {
    return KubernetesSettings.ACTIVATION_KEY;
  }

  private KubernetesSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }


  @Override
  protected KubernetesSensor sensor(CheckFactory checkFactory) {
    return new KubernetesSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, language());
  }

  @Override
  protected String repositoryKey() {
    return KubernetesExtension.REPOSITORY_KEY;
  }

  @Override
  protected KubernetesLanguage language() {
    return new KubernetesLanguage();
  }

  @Override
  protected String fileLanguageKey() {
    return "yaml";
  }
}
