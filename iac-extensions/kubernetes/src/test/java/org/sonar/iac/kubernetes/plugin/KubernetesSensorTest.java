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

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.common.testing.ExtensionSensorTest;

import static org.assertj.core.api.Assertions.assertThat;

class KubernetesSensorTest extends ExtensionSensorTest {

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
  void yaml_files_with_at_least_one_document_with_identifiers_should_be_parsed() {
    analyse(sensor(), inputFile("apiVersion: ~\nkind: ~\nmetadata: ~\n---\n" + K8_IDENTIFIERS));
    assertOneSourceFileIsParsed();
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

  @Test
  @Timeout(20)
  void shouldFastCheckFilePredicate() {
    String content = generateBigJson();
    for (int i = 0; i < 10; i++) {
      InputFile inputFile = inputFile("temp" + i + ".json", content);
      context.fileSystem().add(inputFile);
    }
    long start = System.currentTimeMillis();
    Assertions.assertTimeout(Duration.ofSeconds(1), () -> sensor().execute(context));
    long stop = System.currentTimeMillis();
    System.out.println("shouldFastCheckFilePredicate took: " + (stop - start) + " ms");
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
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0))
      .isEqualTo(message1);
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(1))
      .startsWith(message2);
    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(2);
  }

  private String generateBigJson() {
    StringBuilder sb = new StringBuilder("{\"elements\":[");
    for (int i = 0; i < 500000; i++) {
      sb.append("\"lastName\": \"last name\", \"firstName\": \"first name\", \"streetAddress\": \"street address\", ")
        .append("\"email\": \"email\", \"index\": \"")
        .append(i)
        .append("\"},");
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("]}");
    return sb.toString();
  }
}
