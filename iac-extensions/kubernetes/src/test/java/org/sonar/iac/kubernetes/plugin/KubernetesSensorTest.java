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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.kubernetes.checks.RaiseIssue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
    assertOneSourceFileIsParsed();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Helm content detected in file 'templates/k8.yaml'");
  }

  @Test
  void shouldParseYamlFileWithHelmChartTemplateWhenEnabled() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), true);
    settings.setProperty("sonar.kubernetes.internal.helm.enable", "true");
    context = SensorContextTester.create(baseDir).setSettings(settings);
    var sensor = sensor();
    sensor.execute(context);
    analyse(sensor,
      inputFile(K8_IDENTIFIERS + "foo: {{ .Values.bar }}"),
      inputFile("values.yaml", "bar: var-value"));
    assertOneSourceFileIsParsed();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Helm content detected in file 'templates/k8.yaml'");
  }

  @Test
  void shouldNotParseYamlFileWithoutIdentifiers() {
    analyse(sensor(), inputFile(""));
    assertNotSourceFileIsParsed();
  }

  @Test
  void shouldNotParseYamlFileWithIncompleteIdentifiers() {
    analyse(sensor(), inputFile("apiVersion: ~\nkind: ~\nmetadata: ~\n"));
    assertNotSourceFileIsParsed();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0))
      .startsWith("File without Kubernetes identifier:").endsWith("templates/k8.yaml");
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
    assertThat(descriptor.languages()).containsExactly("yaml");
  }

  @Test
  void shouldNotParseYamlFileWithHelmTemplateDirectives() {
    analyse(sensor(), inputFile(K8_IDENTIFIERS + "{{ .Values.count }}"));
    assertOneSourceFileIsParsed();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Helm content detected in file 'templates/k8.yaml'");
  }

  @Test
  void shouldNotParseFileAndLogAndCatchIOException() throws IOException {
    InputFile inputFile = spy(inputFile(K8_IDENTIFIERS));
    when(inputFile.inputStream()).thenThrow(IOException.class);
    analyse(sensor(), inputFile);

    assertThat(logTester.logs(Level.ERROR)).hasSize(2);
    assertNotSourceFileIsParsed();
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

  @ParameterizedTest
  @MethodSource("provideRaiseIssue")
  void shouldParseHelmAndRaiseIssueOnShiftedLineIssue(RaiseIssue issueRaiser) {
    String originalSourceCode = K8_IDENTIFIERS + "{{ some helm code }}";
    String transformedSourceCode = K8_IDENTIFIERS + "test: produced_line #5\nIssue: Issue #5";
    HelmProcessor helmProcessor = mock(HelmProcessor.class);
    when(helmProcessor.processHelmTemplate(anyString(), eq(originalSourceCode), any())).thenReturn(transformedSourceCode);

    CheckFactory checkFactory = mockCheckFactoryIssueOn(issueRaiser);
    analyse(sensor(helmProcessor, checkFactory), inputFile(originalSourceCode));

    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.primaryLocation().message()).isEqualTo("Issue");

    TextRange textRange = issue.primaryLocation().textRange();
    assertTextRange(textRange, 5, 0, 5, 20);

    assertThat(issue.flows()).isEmpty();
  }

  private static Stream<RaiseIssue> provideRaiseIssue() {
    return Stream.of(
      new RaiseIssue.RaiseIssueOnTextRange(6, 1, 6, 5, "Issue"),
      new RaiseIssue.RaiseIssueOnHasTextRange(6, 1, 6, 5, "Issue"));
  }

  @Test
  void shouldParseTwoHelmFileInARowAndNotMixShiftedLocation() {
    final String originalSourceCode1 = K8_IDENTIFIERS + "{{ long helm code on line 5 }}\n{{ long helm code on line 6 }}";
    final String transformedSourceCode1 = K8_IDENTIFIERS + "new_5_1: compliant #5\nnew_5_2: non_compliant #5\nnew_6_1: compliant #6\nnew_6_2: compliant #6";
    final String originalSourceCode2 = K8_IDENTIFIERS + "{{ helm code on line 5 }}\n{{ helm code on line 6 }}";
    final String transformedSourceCode2 = K8_IDENTIFIERS + "new_5_1: compliant #5\nnew_5_2: compliant #5\nnew_6_1: compliant #6\nnew_6_2: non_compliant #6";

    HelmProcessor helmProcessor = mockHelmProcessor(Map.of(
      originalSourceCode1, transformedSourceCode1,
      originalSourceCode2, transformedSourceCode2));

    var issueRaiser = new RaiseIssue.RaiseIssueOnWord("non_compliant", "Sensitive word 'Issue' detected !");
    CheckFactory checkFactory = mockCheckFactoryIssueOn(issueRaiser);

    analyse(sensor(helmProcessor, checkFactory), inputFile("file1.yaml", originalSourceCode1), inputFile("file2.yaml", originalSourceCode2));
    assertThat(context.allIssues()).hasSize(2);
    Iterator<Issue> iterator = context.allIssues().iterator();
    Issue issue1 = iterator.next();
    Issue issue2 = iterator.next();

    assertThat(issue1.primaryLocation().inputComponent().key()).isEqualTo("moduleKey:file2.yaml");
    assertTextRange(issue1.primaryLocation().textRange(), 6, 0, 6, 25);
    assertThat(issue2.primaryLocation().inputComponent().key()).isEqualTo("moduleKey:file1.yaml");
    assertTextRange(issue2.primaryLocation().textRange(), 5, 0, 5, 30);
  }

  @Test
  void shouldParseHelmAndRaiseIssueNullLocation() {
    String originalSourceCode = K8_IDENTIFIERS + "{{ some helm code }}";
    String transformedSourceCode = K8_IDENTIFIERS + "test: produced_line #5";
    HelmProcessor helmProcessor = mock(HelmProcessor.class);
    when(helmProcessor.processHelmTemplate(anyString(), eq(originalSourceCode), any())).thenReturn(transformedSourceCode);

    var issueRaiser = new RaiseIssue("Issue");
    CheckFactory checkFactory = mockCheckFactoryIssueOn(issueRaiser);
    analyse(sensor(helmProcessor, checkFactory), inputFile(originalSourceCode));

    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.primaryLocation().message()).isEqualTo("Issue");
    TextRange textRange = issue.primaryLocation().textRange();
    assertThat(textRange).isNull();
  }

  @Test
  void shouldParseHelmAndRaiseIssueOnShiftedLineIssueWithSecondaryLocation() {
    String originalSourceCode = K8_IDENTIFIERS + "{{ some helm code }}\n{{ some other helm code }}";
    String transformedSourceCode = K8_IDENTIFIERS + "test: produced_line #5\nIssue: Issue #5\nSecondary: Issue #6";
    HelmProcessor helmProcessor = mock(HelmProcessor.class);
    when(helmProcessor.processHelmTemplate(anyString(), eq(originalSourceCode), any())).thenReturn(transformedSourceCode);

    var secondaryLocation = new SecondaryLocation(TextRanges.range(7, 1, 7, 9), "Secondary message");
    var issueRaiser = new RaiseIssue.RaiseIssueOnSecondaryLocation(6, 1, 6, 5, "Primary message", secondaryLocation);
    CheckFactory checkFactory = mockCheckFactoryIssueOn(issueRaiser);
    analyse(sensor(helmProcessor, checkFactory), inputFile(originalSourceCode));

    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.primaryLocation().message()).isEqualTo("Primary message");
    TextRange textRange = issue.primaryLocation().textRange();
    assertTextRange(textRange, 5, 0, 5, 20);

    assertThat(issue.flows()).hasSize(1);
    Issue.Flow flow1 = issue.flows().get(0);
    assertSecondaryLocation(flow1, 6, 0, 6, 26, "Secondary message");
  }

  @Test
  void shouldParseHelmAndRaiseIssueOnShiftedLineIssueWithMultipleSecondaryLocation() {
    String originalSourceCode = K8_IDENTIFIERS + "{{ some helm code }}\n{{ some other helm code }}\n{{ more helm code... }}";
    String transformedSourceCode = K8_IDENTIFIERS + "test: produced_line #5\nIssue: Issue #5\nSecondary1: Issue #6\nSecondary2: Issue #7";
    HelmProcessor helmProcessor = mock(HelmProcessor.class);
    when(helmProcessor.processHelmTemplate(anyString(), eq(originalSourceCode), any())).thenReturn(transformedSourceCode);

    var secondaryLocation1 = new SecondaryLocation(TextRanges.range(7, 1, 7, 10), "Secondary message 1");
    var secondaryLocation2 = new SecondaryLocation(TextRanges.range(8, 1, 8, 10), "Secondary message 2");
    var issueRaiser = new RaiseIssue.RaiseIssueOnSecondaryLocations(6, 1, 6, 5, "Primary message",
      List.of(secondaryLocation1, secondaryLocation2));
    CheckFactory checkFactory = mockCheckFactoryIssueOn(issueRaiser);
    analyse(sensor(helmProcessor, checkFactory), inputFile(originalSourceCode));

    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.primaryLocation().message()).isEqualTo("Primary message");
    TextRange textRange = issue.primaryLocation().textRange();
    assertTextRange(textRange, 5, 0, 5, 20);

    assertThat(issue.flows()).hasSize(2);

    Issue.Flow flow1 = issue.flows().get(0);
    assertSecondaryLocation(flow1, 6, 0, 6, 26, "Secondary message 1");
    Issue.Flow flow2 = issue.flows().get(1);
    assertSecondaryLocation(flow2, 7, 0, 7, 23, "Secondary message 2");
  }

  private void assertTextRange(@Nullable TextRange textRange, int startLine, int startLineOffset, int endLine, int endLineOffset) {
    assertThat(textRange).isNotNull();
    assertThat(textRange.start().line()).isEqualTo(startLine);
    assertThat(textRange.start().lineOffset()).isEqualTo(startLineOffset);
    assertThat(textRange.end().line()).isEqualTo(endLine);
    assertThat(textRange.end().lineOffset()).isEqualTo(endLineOffset);
  }

  private void assertSecondaryLocation(Issue.Flow flow, int startLine, int startLineOffset, int endLine, int endLineOffset, String message) {
    assertThat(flow.locations()).hasSize(1);
    IssueLocation issueLocation = flow.locations().get(0);
    assertThat(issueLocation.message()).isEqualTo(message);
    assertTextRange(issueLocation.textRange(), startLine, startLineOffset, endLine, endLineOffset);
  }

  private HelmProcessor mockHelmProcessor(Map<String, String> inputToOutput) {
    HelmProcessor helmProcessor = mock(HelmProcessor.class);
    when(helmProcessor.processHelmTemplate(anyString(), anyString(), any())).thenAnswer(input -> inputToOutput.getOrDefault(input.getArgument(1).toString(), ""));
    return helmProcessor;
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

  @Test
  void shouldHaveSpecificSonarlintVisitor() {
    SonarRuntime sonarLintRuntime = SonarRuntimeImpl.forSonarLint(Version.create(6, 0));
    InputFile inputFile = inputFile("file1.tf", "test:val");
    context.setRuntime(sonarLintRuntime);

    analyse(sensor(), inputFile);

    // No highlighting and metrics in SonarLint
    assertThat(context.highlightingTypeAt(inputFile.key(), 1, 0)).isEmpty();
    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC)).isNull();
  }

  private void assertNotSourceFileIsParsed() {
    assertThat(logTester.logs(Level.INFO)).contains("0 source files to be analyzed");
  }

  private void assertOneSourceFileIsParsed() {
    assertThat(logTester.logs(Level.INFO)).contains("1 source file to be analyzed");
  }

  protected InputFile inputFile(String content) {
    return super.inputFile("templates/k8.yaml", content);
  }

  protected InputFile inputFileWithIdentifiers(String content) {
    return super.inputFile("templates/k8.yaml", content + "\n" + K8_IDENTIFIERS);
  }

  @Override
  protected String getActivationSettingKey() {
    return KubernetesSettings.ACTIVATION_KEY;
  }

  private KubernetesSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  private CheckFactory mockCheckFactoryIssueOn(IacCheck issueRaiser) {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks<IacCheck> checks = mock(Checks.class);
    Mockito.<Checks<IacCheck>>when(checkFactory.create(any())).thenReturn(checks);
    when(checks.all()).thenReturn(List.of(issueRaiser));
    when(checks.ruleKey(any())).thenReturn(mock(RuleKey.class));
    return checkFactory;
  }

  @Override
  protected KubernetesSensor sensor(CheckFactory checkFactory) {
    return new KubernetesSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, new KubernetesLanguage(),
      new HelmProcessor(Mockito.mock(HelmEvaluator.class)));
  }

  protected KubernetesSensor sensor(HelmProcessor helmProcessor, CheckFactory checkFactory) {
    return new KubernetesSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, new KubernetesLanguage(), helmProcessor);
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
    String message2 = "org.sonar.iac.common.extension.ParseException: Cannot parse 'templates/k8.yaml:1:1'" +
      System.lineSeparator() +
      "\tat org.sonar.iac.common";
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .isEqualTo(message1);
    assertThat(logTester.logs(Level.DEBUG).get(1))
      .startsWith(message2);
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
  }
}
