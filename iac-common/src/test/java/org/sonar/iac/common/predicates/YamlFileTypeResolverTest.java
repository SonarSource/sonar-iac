/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.predicates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.common.predicates.ArmJsonFilePredicate.ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.ArmJsonFilePredicate.ARM_JSON_FILE_IDENTIFIER_KEY;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_KEY;
import static org.sonar.iac.common.predicates.FilePredicateTestUtils.newInputFileMock;

class YamlFileTypeResolverTest {

  @TempDir
  Path tempDir;

  private static final String VALID_KUBERNETES_CONTENT = """
    apiVersion: v1
    kind: ConfigMap
    metadata:
      name: my-config
    """;
  private static final String VALID_CLOUDFORMATION_CONTENT = "AWSTemplateFormatVersion: var";

  private final YamlFileTypeCache yamlFileTypeCache = new YamlFileTypeCache();
  private Map<FileType, FilePredicate> predicatesPerType;
  private YamlFileTypeResolver yamlFileTypeResolver;

  @BeforeEach
  void setUp() {
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings);
    yamlFileTypeResolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), yamlFileTypeCache);
    predicatesPerType = Map.of(
      FileType.CLOUDFORMATION, yamlFileTypeResolver.getFilePredicate(durationStatistics(), FileType.CLOUDFORMATION),
      FileType.KUBERNETES, yamlFileTypeResolver.getFilePredicate(durationStatistics(), FileType.KUBERNETES),
      FileType.HELM, yamlFileTypeResolver.getFilePredicate(durationStatistics(), FileType.HELM),
      FileType.KUSTOMIZE, yamlFileTypeResolver.getFilePredicate(durationStatistics(), FileType.KUSTOMIZE),
      FileType.JVM_CONFIG, yamlFileTypeResolver.getFilePredicate(durationStatistics(), FileType.JVM_CONFIG),
      FileType.GITHUB_ACTIONS, yamlFileTypeResolver.getFilePredicate(durationStatistics(), FileType.GITHUB_ACTIONS),
      FileType.AZURE_RESOURCE_MANAGER, yamlFileTypeResolver.getFilePredicate(durationStatistics(), FileType.AZURE_RESOURCE_MANAGER));
  }

  // Cloud Formation predicate tests
  @Test
  void shouldMatchCloudFormationPredicate() throws IOException {
    var inputFile = newInputFileMock("file.yaml", VALID_CLOUDFORMATION_CONTENT);
    assertInputFileMatchedOnlyBy(inputFile, FileType.CLOUDFORMATION);
  }

  // Kubernetes predicate tests
  @ParameterizedTest
  @ValueSource(strings = {
    // a plain Kubernetes manifest
    "file.yaml",
    // a JVM config path also resolves to Kubernetes, as the JVM config predicate defers to Kubernetes
    "src/main/resources/vars/application-prod.yaml"
  })
  void shouldMatchKubernetesPredicateRegardlessOfFileName(String path) throws IOException {
    var inputFile = newInputFileMock(path, VALID_KUBERNETES_CONTENT, "kubernetes", InputFile.Type.MAIN);
    assertInputFileMatchedOnlyBy(inputFile, FileType.KUBERNETES);
  }

  @Test
  void shouldMatchKubernetesPredicateOverCloudFormationPredicate() throws IOException {
    var inputFile = newInputFileMock("file.yaml", "%s%n%s".formatted(VALID_KUBERNETES_CONTENT, VALID_CLOUDFORMATION_CONTENT), "kubernetes", InputFile.Type.MAIN);
    assertInputFileMatchedOnlyBy(inputFile, FileType.KUBERNETES);
  }

  // Kustomize predicate tests
  @Test
  void shouldMatchKustomizationPredicate() throws IOException {
    // A kustomization.yaml carries a `resources:` key that would otherwise be picked up by content based predicates;
    // it is resolved to KUSTOMIZE based on its file name (SONARIAC-2859).
    var inputFile = newInputFileMock("kustomization.yaml", "resources:\n  - deployment.yaml");
    assertInputFileMatchedOnlyBy(inputFile, FileType.KUSTOMIZE);
  }

  @Test
  void shouldMatchKustomizationPredicateOverKubernetesPredicate() throws IOException {
    // The Kustomize predicate is resolved first, so a kustomization.yaml is classified as KUSTOMIZE even when it also
    // carries genuine Kubernetes content (apiVersion/kind/metadata), rather than being analyzed as a Kubernetes manifest.
    var inputFile = newInputFileMock("kustomization.yaml", VALID_KUBERNETES_CONTENT, "kubernetes", InputFile.Type.MAIN);
    assertInputFileMatchedOnlyBy(inputFile, FileType.KUSTOMIZE);
  }

  // Helm predicate tests
  @Test
  void shouldMatchHelmPredicate() throws IOException {
    // Helm detection needs real files on disk (the chart folder is located by walking up to Chart.yaml).
    Files.createFile(tempDir.resolve("Chart.yaml"));
    var inputFile = IacTestUtils.inputFile("templates/pod.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml");
    assertInputFileMatchedOnlyBy(inputFile, FileType.HELM);
  }

  // JVM Config predicate tests
  @Test
  void shouldMatchJvmConfigPredicate() throws IOException {
    var inputFile = newInputFileMock("src/main/resources/vars/application-prod.yaml", "");
    assertInputFileMatchedOnlyBy(inputFile, FileType.JVM_CONFIG);
  }

  @Test
  void shouldMatchCloudFormationPredicateOverJvmConfigPredicate() throws IOException {
    // A JVM config file that also looks like a CloudFormation template is analyzed as CloudFormation, as before the
    // file-type detection was centralized (the JVM config sensor used to defer to CloudFormation).
    var inputFile = newInputFileMock("src/main/resources/vars/application-prod.yaml", VALID_CLOUDFORMATION_CONTENT);
    assertInputFileMatchedOnlyBy(inputFile, FileType.CLOUDFORMATION);
  }

  // Github Actions predicate tests
  @Test
  void shouldMatchGithubActionsPredicate() throws IOException {
    var inputFile = newInputFileMock(".github/workflows/deploy.yaml", "");
    assertInputFileMatchedOnlyBy(inputFile, FileType.GITHUB_ACTIONS);
  }

  @Test
  void shouldMatchGithubActionsPredicateOverKubernetesPredicate() throws IOException {
    var inputFile = newInputFileMock(".github/workflows/deploy.yaml", VALID_KUBERNETES_CONTENT, "kubernetes", InputFile.Type.MAIN);
    assertInputFileMatchedOnlyBy(inputFile, FileType.GITHUB_ACTIONS);
  }

  // Azure Resource Manager predicate tests
  @Test
  void shouldMatchArmJsonPredicate() throws IOException {
    var content = "{\"$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\"}";
    var inputFile = newInputFileMock("template.json", content, "json", InputFile.Type.MAIN);
    assertInputFileMatchedOnlyBy(inputFile, FileType.AZURE_RESOURCE_MANAGER);
  }

  @Test
  void shouldNotMatchArmJsonPredicateForYamlFile() throws IOException {
    // The ARM predicate is gated on the JSON language, so a YAML file with an Azure schema identifier is not matched.
    var content = "$schema: https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#";
    var inputFile = newInputFileMock("template.yaml", content, "yaml", InputFile.Type.MAIN);
    predicatesPerType.values().forEach(predicate -> assertThat(predicate.apply(inputFile)).isFalse());
    assertThat(yamlFileTypeCache.get(inputFile.uri())).isEqualTo(FileType.UNDETERMINED);
  }

  @Test
  void shouldStoreInCacheFilesProcessedByPredicates() throws IOException {
    var inputFile = newInputFileMock("src/main/resources/vars/application-prod.yaml", "");
    assertInputFileMatchedOnlyBy(inputFile, FileType.JVM_CONFIG);
    assertThat(yamlFileTypeCache.get(inputFile.uri())).isEqualTo(FileType.JVM_CONFIG);
  }

  @Test
  void shouldStoreUndeterminedInCacheForUnmatchedFiles() throws IOException {
    var inputFile = newInputFileMock("some/plain.yaml", "key: value");
    predicatesPerType.values().forEach(predicate -> assertThat(predicate.apply(inputFile)).isFalse());
    assertThat(yamlFileTypeCache.get(inputFile.uri())).isEqualTo(FileType.UNDETERMINED);
  }

  @Test
  void shouldFailWhenNoFileTypeIsProvided() {
    var statistics = durationStatistics();
    assertThatThrownBy(() -> yamlFileTypeResolver.getFilePredicate(statistics))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("At least one FileType must be provided to build a file predicate");
  }

  @Test
  void shouldReturnInputFilesGroupedByType() {
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings);
    var cloudFormationFile = IacTestUtils.inputFile("cloudformation.yaml", tempDir, VALID_CLOUDFORMATION_CONTENT, "yaml");
    var kubernetesFile = IacTestUtils.inputFile("kubernetes.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml");
    var plainFile = IacTestUtils.inputFile("plain.yaml", tempDir, "key: value", "yaml");
    sensorContext.fileSystem().add(cloudFormationFile);
    sensorContext.fileSystem().add(kubernetesFile);
    sensorContext.fileSystem().add(plainFile);
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());

    assertThat(resolver.getInputFiles(sensorContext.fileSystem(), durationStatistics(), FileType.CLOUDFORMATION)).containsExactly(cloudFormationFile);
    assertThat(resolver.getInputFiles(sensorContext.fileSystem(), durationStatistics(), FileType.KUBERNETES)).containsExactly(kubernetesFile);
    assertThat(resolver.getInputFiles(sensorContext.fileSystem(), durationStatistics(), FileType.CLOUDFORMATION, FileType.KUBERNETES))
      .containsExactlyInAnyOrder(cloudFormationFile, kubernetesFile);
  }

  @Test
  void shouldClassifyFilesReassignedToASpecializedLanguageViaCustomSuffix() {
    // A .yaml file reassigned to a specialized IaC language through `sonar.<lang>.file.suffixes` (so its language is no
    // longer "yaml"/"json") must still be classified by getInputFiles, not dropped from the candidate scope.
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings);
    var cloudFormationFile = IacTestUtils.inputFile("template.yaml", tempDir, VALID_CLOUDFORMATION_CONTENT, "cloudformation");
    var kubernetesFile = IacTestUtils.inputFile("deploy.yaml", tempDir, VALID_KUBERNETES_CONTENT, "kubernetes");
    sensorContext.fileSystem().add(cloudFormationFile);
    sensorContext.fileSystem().add(kubernetesFile);
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());

    assertThat(resolver.getInputFiles(sensorContext.fileSystem(), durationStatistics(), FileType.CLOUDFORMATION)).containsExactly(cloudFormationFile);
    assertThat(resolver.getInputFiles(sensorContext.fileSystem(), durationStatistics(), FileType.KUBERNETES)).containsExactly(kubernetesFile);
  }

  @Test
  void shouldReturnOnlyTheCallingFileSystemsFilesEvenWhenTheCacheIsShared() {
    // A multi-module analysis (sonar.modules) builds one file system per module but shares one cache, so each module's
    // sensor must only receive its own module's files. getInputFiles is therefore scoped to the caller's file system.
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    var sharedCache = new YamlFileTypeCache();

    var moduleA = SensorContextTester.create(tempDir).setSettings(settings);
    var kubernetesA = IacTestUtils.inputFile("moduleA/deploy.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml");
    moduleA.fileSystem().add(kubernetesA);
    var resolverA = new YamlFileTypeResolver(moduleA.fileSystem(), moduleA.config(), sharedCache);

    var moduleB = SensorContextTester.create(tempDir).setSettings(settings);
    var kubernetesB = IacTestUtils.inputFile("moduleB/deploy.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml");
    moduleB.fileSystem().add(kubernetesB);
    var resolverB = new YamlFileTypeResolver(moduleB.fileSystem(), moduleB.config(), sharedCache);

    assertThat(resolverA.getInputFiles(moduleA.fileSystem(), durationStatistics(), FileType.KUBERNETES)).containsExactly(kubernetesA);
    assertThat(resolverB.getInputFiles(moduleB.fileSystem(), durationStatistics(), FileType.KUBERNETES)).containsExactly(kubernetesB);
    // Module B's file is now cached too, but querying module A again must still return only module A's file.
    assertThat(resolverA.getInputFiles(moduleA.fileSystem(), durationStatistics(), FileType.KUBERNETES)).containsExactly(kubernetesA);
  }

  @Test
  void shouldReturnHelmTplTemplatesWhichHaveNoYamlLanguage() throws IOException {
    // A Helm .tpl template is not valid YAML and carries no language, yet it is a HELM file. getInputFiles must return
    // it even though it is not in any candidate language - a language-only candidate filter would drop it.
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings());
    Files.createFile(tempDir.resolve("Chart.yaml"));
    var tplFile = IacTestUtils.inputFile("templates/_helpers.tpl", tempDir, "{{- define \"x\" -}}{{- end -}}", null);
    sensorContext.fileSystem().add(tplFile);
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());

    assertThat(resolver.getInputFiles(sensorContext.fileSystem(), durationStatistics(), FileType.HELM)).containsExactly(tplFile);
  }

  @Test
  void shouldFailWhenNoFileTypeIsProvidedToGetInputFiles() {
    var statistics = durationStatistics();
    var fileSystem = SensorContextTester.create(tempDir).fileSystem();
    assertThatThrownBy(() -> yamlFileTypeResolver.getInputFiles(fileSystem, statistics))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("At least one FileType must be provided to collect files");
  }

  @Test
  void shouldScanTheFileSystemOnlyOnceWhenGetInputFilesIsCalledRepeatedly() {
    // Several getInputFiles calls in one analysis must trigger the scan only once; the rest reuse the memoized result.
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings);
    sensorContext.fileSystem().add(IacTestUtils.inputFile("cloudformation.yaml", tempDir, VALID_CLOUDFORMATION_CONTENT, "yaml"));
    sensorContext.fileSystem().add(IacTestUtils.inputFile("kubernetes.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml"));
    var fileSystem = spy(sensorContext.fileSystem());
    var resolver = new YamlFileTypeResolver(fileSystem, sensorContext.config(), new YamlFileTypeCache());

    resolver.getInputFiles(fileSystem, durationStatistics(), FileType.CLOUDFORMATION);
    resolver.getInputFiles(fileSystem, durationStatistics(), FileType.KUBERNETES);
    resolver.getInputFiles(fileSystem, durationStatistics(), FileType.CLOUDFORMATION, FileType.KUBERNETES);

    verify(fileSystem, times(1)).inputFiles(any());
  }

  private DurationStatistics durationStatistics() {
    return new DurationStatistics(mock(Configuration.class));
  }

  void assertInputFileMatchedOnlyBy(InputFile inputFile, FileType filePredicateType) {
    assertThat(predicatesPerType.get(filePredicateType).apply(inputFile))
      .describedAs("'%s' file should have matched predicate %s".formatted(inputFile, filePredicateType))
      .isTrue();
    predicatesPerType.entrySet().stream()
      .filter(entry -> entry.getKey() != filePredicateType)
      .forEach(entry -> assertThat(entry.getValue().apply(inputFile))
        .describedAs("'%s' file should NOT have matched predicate %s".formatted(inputFile, entry.getKey()))
        .isFalse());
  }
}
