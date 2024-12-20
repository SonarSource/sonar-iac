/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.utils.OperatingSystemUtils;
import org.sonar.iac.kubernetes.checks.RaiseIssue;
import org.sonar.iac.kubernetes.visitors.ProjectContextImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.SONARLINT_RUNTIME_9_9;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;
import static org.sonar.iac.kubernetes.KubernetesAssertions.assertThat;

class KubernetesSensorTest extends ExtensionSensorTest {

  private static final String K8_IDENTIFIERS = "apiVersion: ~\nkind: ~\nmetadata: ~\n";
  private static final String PARSING_ERROR_KEY = "S2260";
  private final SonarLintFileListener sonarLintFileListener = mock(SonarLintFileListener.class);

  @BeforeEach
  void setUp() {
    when(sonarLintFileListener.getProjectContext()).thenReturn(new ProjectContextImpl());
  }

  @Test
  void shouldParseYamlFileWithKubernetesIdentifiers() {
    analyze(sensor(), inputFile(K8_IDENTIFIERS));
    assertOneSourceFileIsParsed();
    verifyLinesOfCodeTelemetry(3);
  }

  @Test
  void shouldParseMultipleYamlFileWithKubernetesIdentifiers() {
    analyze(sensor(), inputFile("templates/file_1.yaml", K8_IDENTIFIERS), inputFile("templates/file_2.yaml", K8_IDENTIFIERS));
    assertNSourceFileIsParsed(2);
    verifyLinesOfCodeTelemetry(6);
  }

  @Test
  void shouldNotParseYamlFileWithHelmChartTemplate() {
    analyze(sensor(), inputFile(K8_IDENTIFIERS + "foo: {{ .bar }}"));
    assertOneSourceFileIsParsed();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Helm content detected in file 'templates/k8.yaml'");
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldParseYamlFileWithHelmChartTemplate() {
    var sensor = sensor();
    analyze(sensor,
      inputFile(K8_IDENTIFIERS + "foo: {{ .Values.bar }}"),
      inputFile("values.yaml", "bar: var-value"),
      inputFile("Chart.yaml", "name: foo"));
    assertOneSourceFileIsParsed();

    var logs = logTester.logs();
    assertThat(logs)
      .contains("Helm content detected in file 'templates/k8.yaml'")
      .contains("Initializing Helm processor")
      .contains("Kubernetes Parsing Statistics: Pure Kubernetes files count: 0, parsed: 0, not parsed: 0; " +
        "Helm files count: 1, parsed: 0, not parsed: 1; Kustomize file count: 0")
      .doesNotContain("Skipping initialization of Helm processor");
    // TODO: SONARIAC-1877 Fix logic inside this test
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldNotParseYamlFileWithHelmChartTemplateWhenDisabled() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), true);
    settings.setProperty("sonar.kubernetes.internal.helm.enable", "false");
    context.setSettings(settings);
    var sensor = sensor();
    analyze(sensor,
      inputFile(K8_IDENTIFIERS + "foo: {{ .Values.bar }}"),
      inputFile("values.yaml", "bar: var-value"));
    assertOneSourceFileIsParsed();

    var logs = logTester.logs();
    assertThat(logs)
      .contains("Skipping initialization of Helm processor")
      .doesNotContain("Initializing Helm processor");
  }

  @Test
  void shouldNotParseYamlFileWithHelmChartTemplateInSonarLintContext() {
    analyze(sonarLintContext, sonarLintSensor(),
      inputFile(K8_IDENTIFIERS + "foo: {{ .Values.bar }}"),
      inputFile("values.yaml", "bar: var-value"));
    assertOneSourceFileIsParsed();

    var logs = logTester.logs();
    assertThat(logs)
      .contains("Initializing Helm processor")
      .doesNotContain("Skipping initialization of Helm processor");
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldNotParseYamlFileWithHelmChartTemplateInSonarLintContextAndHelmAnalysisDisabled() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), true);
    settings.setProperty("sonar.kubernetes.internal.helm.enable", "false");
    sonarLintContext.setSettings(settings);
    analyze(sonarLintContext, sonarLintSensor(),
      inputFile(K8_IDENTIFIERS + "foo: {{ .Values.bar }}"),
      inputFile("values.yaml", "bar: var-value"));
    assertOneSourceFileIsParsed();

    var logs = logTester.logs();
    assertThat(logs)
      .contains("Skipping initialization of Helm processor")
      .doesNotContain("Initializing Helm processor");
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldNotParseYamlFileWithHelmChartTemplateWhenRunOnUnsupportedPlatform() {
    try (var ignored = Mockito.mockStatic(OperatingSystemUtils.class)) {
      when(OperatingSystemUtils.getCurrentPlatformIfSupported()).thenReturn(Optional.empty());
      var sensor = sensor();
      analyze(sensor,
        inputFile(K8_IDENTIFIERS + "foo: {{ .Values.bar }}"),
        inputFile("values.yaml", "bar: var-value"));
      assertOneSourceFileIsParsed();

      var logs = logTester.logs();
      assertThat(logs)
        .contains("Skipping initialization of Helm processor")
        .doesNotContain("Initializing Helm processor");
      // Kubernetes file parsing does succeed
      verifyLinesOfCodeTelemetry(4);
    }
  }

  @Test
  void shouldNotParseYamlFileWithoutIdentifiers() {
    analyze(sensor(), inputFile(""));
    assertNotSourceFileIsParsed();
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldNotParseYamlFileWithIncompleteIdentifiers() {
    analyze(sensor(), inputFile("apiVersion: ~\nkind: ~\n"));
    assertNotSourceFileIsParsed();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).hasSize(3);
    assertThat(logs.get(1)).isEqualTo("Initializing Helm processor");
    assertThat(logs.get(2))
      .startsWith("File without Kubernetes identifier:").endsWith("templates/k8.yaml");
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldParseYamlFilesWithinSingleStream() {
    analyze(sensor(), inputFile(K8_IDENTIFIERS + "---\n" + K8_IDENTIFIERS));
    assertOneSourceFileIsParsed();
    assertThat(logTester.logs(Level.DEBUG))
      .contains("Kubernetes Parsing Statistics: Pure Kubernetes files count: 1, parsed: 1, not parsed: 0; " +
        "Helm files count: 0, parsed: 0, not parsed: 0; Kustomize file count: 0");
    verifyLinesOfCodeTelemetry(7);
  }

  @Test
  void shouldParseYamlFilesWithAtLeastOneDocumentWithIdentifiers() {
    analyze(sensor(), inputFile("apiVersion: ~\nkind: ~\nmetadata: ~\n---\n" + K8_IDENTIFIERS));
    assertOneSourceFileIsParsed();
    assertThat(logTester.logs(Level.DEBUG))
      .contains("Kubernetes Parsing Statistics: Pure Kubernetes files count: 1, parsed: 1, not parsed: 0; " +
        "Helm files count: 0, parsed: 0, not parsed: 0; Kustomize file count: 0");
    verifyLinesOfCodeTelemetry(7);
  }

  @Test
  void shouldRaiseParsingIssueForYamlFileWithRecursiveAnchorReference() {
    analyze(sensor(checkFactory(PARSING_ERROR_KEY)), inputFileWithIdentifiers("foo: &fooanchor\n" +
      " bar: *fooanchor"));

    assertThat(context.allAnalysisErrors()).hasSize(1);
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo(PARSING_ERROR_KEY);
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldReturnKubernetesDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Kubernetes Sensor");
    assertThat(descriptor.languages()).containsExactly("yaml");
    assertThat(descriptor.isProcessesFilesIndependently()).isFalse();
  }

  @Test
  void shouldNotParseYamlFileWithHelmTemplateDirectives() {
    analyze(sensor(),
      inputFile(K8_IDENTIFIERS + "{{ .Values.count }}"));
    assertOneSourceFileIsParsed();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Helm content detected in file 'templates/k8.yaml'");
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldNotParseFileAndLogAndCatchIOException() throws IOException {
    InputFile inputFile = spy(inputFile(K8_IDENTIFIERS));
    when(inputFile.inputStream()).thenThrow(IOException.class);
    analyze(sensor(), inputFile);

    assertThat(logTester.logs(Level.ERROR)).hasSize(2);
    assertNotSourceFileIsParsed();
    verifyLinesOfCodeTelemetry(0);
  }

  // TODO: SONARIAC-1877 Fix logic inside this test, some of the files are parseable, some not
  @ParameterizedTest
  @ValueSource(strings = {
    "'{{ .Values.count }}'",
    "\"{{ .Values.count }}\"",
    "# {{ .Values.count }}",
    "custom-label: {{MY_CUSTOM_LABEL}}"
  })
  void shouldParseYamlFileWithHelmTemplateDirectives(String content) {
    analyze(sensor(), inputFile(K8_IDENTIFIERS + content));
    assertOneSourceFileIsParsed();
  }

  @ParameterizedTest
  @MethodSource("provideRaiseIssue")
  void shouldParseHelmAndRaiseIssueOnShiftedLineIssue(RaiseIssue issueRaiser) {
    String originalSourceCode = K8_IDENTIFIERS + "{{ some helm code }}";
    String transformedSourceCode = K8_IDENTIFIERS + "test: produced_line #4\nIssue: Issue #4";
    HelmProcessor helmProcessor = new TestHelmProcessor(transformedSourceCode);
    CheckFactory checkFactory = mockCheckFactoryIssueOn(issueRaiser);
    analyze(sensor(helmProcessor, checkFactory), inputFile(originalSourceCode));

    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.primaryLocation().message()).isEqualTo("Issue");

    TextRange textRange = issue.primaryLocation().textRange();
    assertThat(textRange).hasRange(4, 1, 4, 5);

    assertThat(issue.flows()).isEmpty();
    assertThat(logTester.logs(Level.DEBUG))
      .contains("Kubernetes Parsing Statistics: Pure Kubernetes files count: 0, parsed: 0, not parsed: 0; " +
        "Helm files count: 1, parsed: 1, not parsed: 0; Kustomize file count: 0");
    verifyLinesOfCodeTelemetry(4);
  }

  private static Stream<RaiseIssue> provideRaiseIssue() {
    return Stream.of(
      new RaiseIssue.RaiseIssueOnTextRange(5, 1, 5, 5, "Issue"),
      new RaiseIssue.RaiseIssueOnHasTextRange(5, 1, 5, 5, "Issue"));
  }

  @Test
  void shouldParseTwoHelmFileInARowAndNotMixShiftedLocation() {
    final String originalSourceCode1 = K8_IDENTIFIERS + "{{ long helm code on line 5 }}\n{{ long helm code on line 6 }}";
    final String transformedSourceCode1 = K8_IDENTIFIERS + """
      new_5_1: compliant #4
      new_5_2: non_compliant #4
      new_6_1: compliant #5
      new_6_2: compliant #5""";
    final String originalSourceCode2 = K8_IDENTIFIERS + "{{ helm code on line 5 }}\n{{ helm code on line 6 }}";
    final String transformedSourceCode2 = K8_IDENTIFIERS + """
      new_5_1: compliant #4
      new_5_2: compliant #4
      new_6_1: compliant #5
      new_6_2: non_compliant #5""";

    HelmProcessor helmProcessor = new TestHelmProcessor(Map.of(
      originalSourceCode1, transformedSourceCode1,
      originalSourceCode2, transformedSourceCode2));

    var issueRaiser = new RaiseIssue.RaiseIssueOnWord("non_compliant", "Sensitive word 'Issue' detected !");
    CheckFactory checkFactory = mockCheckFactoryIssueOn(issueRaiser);

    KubernetesSensor sensor = sensor(helmProcessor, checkFactory);
    InputFile inputFile1 = inputFile("file1.yaml", originalSourceCode1);
    InputFile inputFile2 = inputFile("file2.yaml", originalSourceCode2);

    analyze(sensor, inputFile1, inputFile2);
    assertThat(context.allIssues()).hasSize(2);
    Iterator<Issue> iterator = context.allIssues().iterator();
    Issue issue1 = iterator.next();
    Issue issue2 = iterator.next();

    assertThat(issue1.primaryLocation().inputComponent().key()).isEqualTo("moduleKey:file2.yaml");
    assertThat(issue1.primaryLocation().textRange()).hasRange(5, 9, 5, 22);
    assertThat(issue2.primaryLocation().inputComponent().key()).isEqualTo("moduleKey:file1.yaml");
    assertThat(issue2.primaryLocation().textRange()).hasRange(4, 9, 4, 22);
    assertThat(logTester.logs(Level.DEBUG))
      .contains("Kubernetes Parsing Statistics: Pure Kubernetes files count: 0, parsed: 0, not parsed: 0; " +
        "Helm files count: 2, parsed: 2, not parsed: 0; Kustomize file count: 0");
    verifyLinesOfCodeTelemetry(10);
  }

  @Test
  void shouldParseHelmAndRaiseIssueNullLocation() {
    String originalSourceCode = K8_IDENTIFIERS + "{{ some helm code }}";
    String transformedSourceCode = K8_IDENTIFIERS + "test: produced_line #5";
    var helmProcessor = mock(HelmProcessor.class);
    when(helmProcessor.processHelmTemplate(eq(originalSourceCode), any())).thenReturn(transformedSourceCode);

    var issueRaiser = new RaiseIssue("Issue");
    CheckFactory checkFactory = mockCheckFactoryIssueOn(issueRaiser);
    var sensor = sensor(helmProcessor, checkFactory);
    analyze(sensor, inputFile(originalSourceCode));

    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.primaryLocation().message()).isEqualTo("Issue");
    TextRange textRange = issue.primaryLocation().textRange();
    Assertions.assertThat(textRange).isNull();
    verifyLinesOfCodeTelemetry(4);
  }

  @Test
  void shouldParseHelmAndRaiseIssueOnShiftedLineIssueWithSecondaryLocation() {
    String originalSourceCode = K8_IDENTIFIERS + "{{ some helm code }}\n{{ some other helm code }}";
    String transformedSourceCode = K8_IDENTIFIERS + "test: produced_line #4\nIssue: Issue #4\nSecondary: Issue #5";
    HelmProcessor helmProcessor = new TestHelmProcessor(transformedSourceCode);

    var secondaryLocation = new SecondaryLocation(TextRanges.range(6, 1, 6, 9), "Secondary message");
    var issueRaiser = new RaiseIssue.RaiseIssueOnSecondaryLocation(5, 1, 5, 5, "Primary message", secondaryLocation);
    CheckFactory checkFactory = mockCheckFactoryIssueOn(issueRaiser);
    analyze(sensor(helmProcessor, checkFactory), inputFile(originalSourceCode));

    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.primaryLocation().message()).isEqualTo("Primary message");
    TextRange textRange = issue.primaryLocation().textRange();
    assertThat(textRange)
      .on(originalSourceCode)
      .isEqualTo("{ so")
      .hasRange(4, 1, 4, 5);

    assertThat(issue.flows()).hasSize(1);
    Issue.Flow flow1 = issue.flows().get(0);
    assertSecondaryLocation(flow1, 5, 0, 5, 26, "Secondary message");
    assertThat(flow1.locations().get(0).textRange())
      .on(originalSourceCode)
      .isEqualTo("{{ some other helm code }}");
    verifyLinesOfCodeTelemetry(5);
  }

  @Test
  void shouldParseHelmAndRaiseIssueOnShiftedLineIssueWithMultipleSecondaryLocation() {
    String originalSourceCode = K8_IDENTIFIERS + "{{ some helm code }}\n{{ some other helm code }}\n{{ more helm code... }}";
    String transformedSourceCode = K8_IDENTIFIERS + "test: produced_line #4\nIssue: Issue #4\nSecondary1: Issue #5\nSecondary2: Issue #6";
    HelmProcessor helmProcessor = new TestHelmProcessor(transformedSourceCode);

    var secondaryLocation1 = new SecondaryLocation(TextRanges.range(6, 1, 6, 10), "Secondary message 1");
    var secondaryLocation2 = new SecondaryLocation(TextRanges.range(7, 1, 7, 10), "Secondary message 2");
    var issueRaiser = new RaiseIssue.RaiseIssueOnSecondaryLocations(5, 1, 5, 5, "Primary message",
      List.of(secondaryLocation1, secondaryLocation2));
    CheckFactory checkFactory = mockCheckFactoryIssueOn(issueRaiser);
    analyze(sensor(helmProcessor, checkFactory), inputFile(originalSourceCode));

    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.primaryLocation().message()).isEqualTo("Primary message");
    TextRange textRange = issue.primaryLocation().textRange();
    assertThat(textRange).hasRange(4, 1, 4, 5);

    assertThat(issue.flows()).hasSize(2);

    Issue.Flow flow1 = issue.flows().get(0);
    assertSecondaryLocation(flow1, 5, 0, 5, 26, "Secondary message 1");
    Issue.Flow flow2 = issue.flows().get(1);
    assertSecondaryLocation(flow2, 6, 0, 6, 23, "Secondary message 2");
    verifyLinesOfCodeTelemetry(6);
  }

  private void assertSecondaryLocation(Issue.Flow flow, int startLine, int startLineOffset, int endLine, int endLineOffset,
    String message) {
    assertThat(flow.locations()).hasSize(1);
    IssueLocation issueLocation = flow.locations().get(0);
    assertThat(issueLocation.message()).isEqualTo(message);
    assertThat(issueLocation.textRange()).hasRange(startLine, startLineOffset, endLine, endLineOffset);
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

    FilePredicate filePredicate = sensor().customFilePredicate(context, new DurationStatistics(mock(Configuration.class)));
    assertThat(filePredicate.apply(largeFileWithIdentifier)).isFalse();
    assertThat(filePredicate.apply(mediumFileWithIdentifier)).isTrue();
  }

  @Test
  void shouldHaveSpecificSonarlintVisitor() {
    InputFile inputFile = inputFile("file1.tf", "test:val");
    context.setRuntime(SONARLINT_RUNTIME_9_9);

    analyze(sensor(), inputFile);

    // No highlighting and metrics in SonarLint
    assertThat(context.highlightingTypeAt(inputFile.key(), 1, 0)).isEmpty();
    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC)).isNull();
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldDetectHelmFiles() {
    var context = SensorContextTester.create(Path.of("src/test/resources").toAbsolutePath());

    InputFile pod1 = IacTestUtils.inputFile("helm/templates/pod.yaml", "yaml");
    InputFile pod2 = IacTestUtils.inputFile("helm/templates/nested/pod.yaml", "yaml");
    InputFile pod3 = IacTestUtils.inputFile("helm/templates/nested/double-nested/pod.yaml", "yaml");
    InputFile pod4 = IacTestUtils.inputFile("helm/templates/no-identifiers.yaml", "yaml");

    FilePredicate filePredicate = sensor().customFilePredicate(context, new DurationStatistics(mock(Configuration.class)));
    assertThat(filePredicate.apply(pod1)).isTrue();
    assertThat(filePredicate.apply(pod2)).isTrue();
    assertThat(filePredicate.apply(pod3)).isTrue();
    assertThat(filePredicate.apply(pod4)).isTrue();
  }

  @Test
  void shouldDetectValuesYamlFile() {
    var context = SensorContextTester.create(Path.of("src/test/resources").toAbsolutePath());
    InputFile valuesFile = IacTestUtils.inputFile("helm/values.yaml", "yaml");
    InputFile valuesFile2 = IacTestUtils.inputFile("helm/values.yml", "yaml");

    FilePredicate filePredicate = sensor().customFilePredicate(context, new DurationStatistics(mock(Configuration.class)));
    assertThat(filePredicate.apply(valuesFile)).isTrue();
    assertThat(filePredicate.apply(valuesFile2)).isTrue();
  }

  @Test
  void shouldDetectChartYamlFile() {
    var context = SensorContextTester.create(Path.of("src/test/resources").toAbsolutePath());
    InputFile valuesFile = IacTestUtils.inputFile("helm/Chart.yaml", "yaml");
    InputFile valuesFile2 = IacTestUtils.inputFile("helm/Chart.yml", "yaml");

    FilePredicate filePredicate = sensor().customFilePredicate(context, new DurationStatistics(mock(Configuration.class)));
    assertThat(filePredicate.apply(valuesFile)).isTrue();
    // only Chart.yaml is accepted by helm command, the Chart.yml is invalid and not recognized as Chart directory
    assertThat(filePredicate.apply(valuesFile2)).isFalse();
  }

  @Test
  void shouldDetectTplFile() {
    var context = SensorContextTester.create(Path.of("src/test/resources").toAbsolutePath());
    InputFile tplFile = IacTestUtils.inputFile("helm/templates/_helpers.tpl", (String) null);

    FilePredicate filePredicate = sensor().customFilePredicate(context, new DurationStatistics(mock(Configuration.class)));
    assertThat(filePredicate.apply(tplFile)).isTrue();
  }

  @Test
  void shouldAnalyzeAndLogKustomizeFiles() {
    InputFile kustomizeHelm = inputFile("templates/kustomization.yaml", "{{ some helm code }}\n" + K8_IDENTIFIERS);
    InputFile kustomizeK8s = inputFile("templates/kustomization.yml", K8_IDENTIFIERS);

    analyze(sensor(), kustomizeHelm, kustomizeK8s);
    assertNSourceFileIsParsed(2);
    assertThat(logTester.logs(Level.DEBUG))
      .contains("Kubernetes Parsing Statistics: Pure Kubernetes files count: 1, parsed: 1, not parsed: 0; " +
        "Helm files count: 1, parsed: 0, not parsed: 1; Kustomize file count: 2");
    // helm file is not parseable, only kustomize file is counted
    verifyLinesOfCodeTelemetry(3);
  }

  @Test
  void shouldStoreTelemetry() {
    var kustomizeHelm = inputFile("templates/kustomization.yaml", "{{ some helm code }}\n" + K8_IDENTIFIERS);
    var kustomizeK8s = inputFile("templates/kustomization.yml", K8_IDENTIFIERS);

    analyze(context, sensor(), kustomizeHelm, kustomizeK8s);

    verify(context).addTelemetryProperty("iac.helm", "1");
    verify(context).addTelemetryProperty("iac.kustomize", "1");
    // the helm files throws a parsing error and is not counted towards loc
    verifyLinesOfCodeTelemetry(3);
  }

  @Test
  void shouldStoreEmptyTelemetry() {
    var helmFile = inputFile("templates/pod.yaml", "{{ some helm code }}\n" + K8_IDENTIFIERS);
    var k8sFile = inputFile("templates/pod.yml", K8_IDENTIFIERS);

    analyze(context, sensor(), helmFile, k8sFile);

    verify(context).addTelemetryProperty("iac.helm", "1");
    verify(context).addTelemetryProperty("iac.kustomize", "0");
    // the helm files throws a parsing error and is not counted towards loc
    verifyLinesOfCodeTelemetry(3);
  }

  @Test
  void shouldNotStoreTelemetryWhenNoK8sFiles() {
    var file1 = inputFile("templates/not-a-k8s.yaml", "{{ some helm code }}\n");
    var file2 = inputFile("not-a-k8s.yml", "foo: bar");

    analyze(context, sensor(), file1, file2);

    verify(context, never()).addTelemetryProperty(eq("iac.helm"), anyString());
    verify(context, never()).addTelemetryProperty(eq("iac.kustomize"), anyString());
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldInitSonarLintFileListener() {
    var inputFile = inputFile("templates/kustomization.yaml", K8_IDENTIFIERS);

    analyze(sensorSonarLint(), inputFile);

    verify(sonarLintFileListener).initContext(any(), any());
  }

  @Test
  void shouldLogPredicateInDurationStatistics() {
    settings.setProperty("sonar.iac.duration.statistics", "true");

    InputFile kustomizeHelm = inputFile("templates/kustomization.yaml", "{{ some helm code }}\n" + K8_IDENTIFIERS);
    InputFile kustomizeK8s = inputFile("templates/kustomization.yml", K8_IDENTIFIERS);

    analyze(sensor(), kustomizeHelm, kustomizeK8s);
    assertThat(durationStatisticLog()).contains("KubernetesOrHelmFilePredicate");
  }

  private void assertNotSourceFileIsParsed() {
    assertNSourceFileIsParsed(0);
  }

  private void assertOneSourceFileIsParsed() {
    assertNSourceFileIsParsed(1);
  }

  private void assertNSourceFileIsParsed(int fileQuantity) {
    String file = pluralizeFile(fileQuantity);
    String has = pluralizeHas(fileQuantity);
    List<String> expectedLogs = new ArrayList<>();
    expectedLogs.add(fileQuantity + " source " + file + " to be parsed");
    expectedLogs.add(fileQuantity + "/" + fileQuantity + " source " + file + " " + has + " been parsed");
    expectedLogs.add(fileQuantity + " source " + file + " to be analyzed");
    expectedLogs.add(fileQuantity + "/" + fileQuantity + " source " + file + " " + has + " been analyzed");
    expectedLogs.add(fileQuantity + " source " + file + " to be checked");
    expectedLogs.add(fileQuantity + "/" + fileQuantity + " source " + file + " " + has + " been checked");
    assertThat(logTester.logs(Level.INFO)).contains(expectedLogs.toArray(new String[0]));
  }

  private static String pluralizeFile(long count) {
    return count == 1L ? "file" : "files";
  }

  private static String pluralizeHas(long count) {
    return count == 1L ? "has" : "have";
  }

  protected InputFile inputFile(String content) {
    return inputFile("templates/k8.yaml", content);
  }

  protected InputFile inputFileWithIdentifiers(String content) {
    return inputFile("templates/k8.yaml", content + "\n" + K8_IDENTIFIERS);
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
    return new KubernetesSensor(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION, fileLinesContextFactory, checkFactory, noSonarFilter, new KubernetesLanguage(),
      mock(HelmEvaluator.class));
  }

  protected KubernetesSensor sensorSonarLint() {
    return new KubernetesSensor(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION, fileLinesContextFactory, checkFactory(), noSonarFilter, new KubernetesLanguage(),
      mock(HelmEvaluator.class), sonarLintFileListener);
  }

  protected KubernetesSensor sensor(HelmProcessor helmProcessor, CheckFactory checkFactory) {
    var sensor = sensor(checkFactory);
    sensor.setHelmProcessorForTesting(helmProcessor);
    return sensor;
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
  protected Map<InputFile, Integer> validFilesMappedToExpectedLoCs() {
    return Map.of(
      validFile(), 3,
      inputFile("templates/k82.yaml", "\n" + K8_IDENTIFIERS), 3);
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    String message1 = """
      mapping values are not allowed here
       in reader, line 1, column 5:
          a: b: c
              ^
      """;
    String message2 = "org.sonar.iac.common.extension.ParseException: Cannot parse 'templates/k8.yaml:1:1'" +
      System.lineSeparator() +
      "\tat org.sonar.iac.common";
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .isEqualTo("Checking conditions for enabling Helm analysis: isHelmActivationFlagTrue=true, " +
        "isHelmEvaluatorExecutableAvailable=true");
    assertThat(logTester.logs(Level.DEBUG).get(1))
      .isEqualTo("Initializing Helm processor");
    assertThat(logTester.logs(Level.DEBUG).get(2))
      .isEqualTo(message1);
    assertThat(logTester.logs(Level.DEBUG).get(3))
      .startsWith(message2);
    assertThat(logTester.logs(Level.DEBUG).get(4))
      .startsWith("Kubernetes Parsing Statistics");
    assertThat(logTester.logs(Level.DEBUG)).hasSize(5);
  }

  private KubernetesSensor sonarLintSensor(String... rules) {
    return new KubernetesSensor(
      SONARLINT_RUNTIME_9_9,
      fileLinesContextFactory,
      checkFactory(sonarLintContext, rules),
      noSonarFilter,
      new KubernetesLanguage(),
      mock(HelmEvaluator.class));
  }
}
