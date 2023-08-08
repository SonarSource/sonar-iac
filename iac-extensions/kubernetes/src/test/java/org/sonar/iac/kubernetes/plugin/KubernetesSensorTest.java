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
package org.sonar.iac.kubernetes.plugin;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class KubernetesSensorTest extends ExtensionSensorTest {

  private static final String K8_IDENTIFIERS = "apiVersion: ~\nkind: ~\nmetadata: ~\nspec: ~\n";
  private static final String PARSING_ERROR_KEY = "S2260";

  @Test
  void shouldParseYamlFileWithIdentifiers() {
    analyse(sensor(), inputFile(K8_IDENTIFIERS));
    assertOneSourceFileIsParsed();
  }

  @Test
  void shouldNotParseYamlFileWithHelmChartTemplate() {
    analyse(sensor(), inputFile(K8_IDENTIFIERS + "foo: {{ .bar }}"));
    asserNotSourceFileIsParsed();
  }

  @Test
  void shouldNotParseYamlFileWithoutIdentifiers() {
    analyse(sensor(), inputFile(""));
    asserNotSourceFileIsParsed();
  }

  @Test
  void shouldNotParseYamlFileWithIncompleteIdentifiers() {
    analyse(sensor(), inputFile("apiVersion: ~\nkind: ~\nmetadata: ~\n"));
    asserNotSourceFileIsParsed();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0))
      .startsWith("File without Kubernetes identifier:").endsWith("k8.yaml");
  }

  @Test
  void shouldParseYamlFilesWithinSingleStream() {
    analyse(sensor(), inputFile(K8_IDENTIFIERS + "---\n" + K8_IDENTIFIERS));
    assertOneSourceFileIsParsed();
  }

  @Test
  void shouldParseYamlFilesWithAtLeastOneDocumentWithIdentifiers() {
    analyse(sensor(), inputFile("apiVersion: ~\nkind: ~\nmetadata: ~\n---\n" + K8_IDENTIFIERS));
    assertOneSourceFileIsParsed();
  }

  @Test
  void shouldRaiseParsingIssueForYamlFileWithRecursiveAnchorReference() {
    analyse(sensor(checkFactory(PARSING_ERROR_KEY)), inputFileWithIdentifiers("foo: &fooanchor\n" +
      " bar: *fooanchor"));

    assertThat(context.allAnalysisErrors()).hasSize(1);
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo(PARSING_ERROR_KEY);
  }

  @Test
  void shouldReturnKubernetesDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Kubernetes Sensor");
    assertThat(descriptor.languages()).containsExactly("json", "yaml");
  }

  @Test
  void shouldNotParseYamlFileWithHelmTemplateDirectives() {
    analyse(sensor(), inputFile(K8_IDENTIFIERS + "{{ .Values.count }}"));
    asserNotSourceFileIsParsed();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "'{{ .Values.count }}'",
    "\"{{ .Values.count }}\"",
    "# {{ .Values.count }}",
    "custom-label: {{MY_CUSTOM_LABEL}}"
  })
  void shouldParseYamlFileWithHelmTemplateDirectives(String content) {
    analyse(sensor(), inputFile(K8_IDENTIFIERS + content));
    assertOneSourceFileIsParsed();
  }

  /**
   * When identifying whether an input file is a Kubernetes file, various identifiers are retrieved from the file.
   * In order not to spend too much time on this verification in large files, it is only applied to the first 8kb.
   * This test checks 2 files with valid identifiers. The identifiers are at the end of each file.
   */
  @Test
  void shouldFastCheckFilePredicate() {
    InputFile largeFileWithIdentifier = IacTestUtils.inputFile("large_file_with_identifier.yaml", "yaml");
    InputFile mediumFileWithIdentifier = IacTestUtils.inputFile("medium_file_with_identifier.yaml", "yaml");

    FilePredicate filePredicate = sensor().customFilePredicate(context);
    assertThat(filePredicate.apply(largeFileWithIdentifier)).isFalse();
    assertThat(filePredicate.apply(mediumFileWithIdentifier)).isTrue();
  }

  private void asserNotSourceFileIsParsed() {
    assertThat(logTester.logs(Level.INFO)).contains("0 source files to be analyzed");
  }

  private void assertOneSourceFileIsParsed() {
    assertThat(logTester.logs(Level.INFO)).contains("1 source file to be analyzed");
  }

  protected InputFile inputFile(String content) {
    return super.inputFile("k8.yaml", content);
  }

  protected InputFile inputFileWithIdentifiers(String content) {
    return super.inputFile("k8.yaml", content + "\n" + K8_IDENTIFIERS);
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
    return new KubernetesSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, new KubernetesLanguage());
  }

  @Override
  protected String repositoryKey() {
    return KubernetesExtension.REPOSITORY_KEY;
  }

  @Override
  protected String fileLanguageKey() {
    return "yaml";
  }

  @Override
  protected InputFile emptyFile() {
    return inputFile("k8.yaml", "");
  }

  @Override
  protected InputFile fileWithParsingError() {
    return inputFileWithIdentifiers("a: b: c");
  }

  @Override
  protected InputFile validFile() {
    return inputFileWithIdentifiers("");
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    String message1 = "mapping values are not allowed here\n" +
      " in reader, line 1, column 5:\n" +
      "    a: b: c\n" +
      "        ^\n";
    String message2 = "org.sonar.iac.common.extension.ParseException: Cannot parse 'k8.yaml:1:1'" +
      System.lineSeparator() +
      "\tat org.sonar.iac.common";
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .isEqualTo(message1);
    assertThat(logTester.logs(Level.DEBUG).get(1))
      .startsWith(message2);
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
  }
}
