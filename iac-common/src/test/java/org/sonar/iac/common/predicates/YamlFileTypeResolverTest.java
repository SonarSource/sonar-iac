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

import com.sonarsource.scanner.engine.sensor.test.fixtures.SensorContextTester;
import com.sonarsource.scanner.engine.sensor.test.fixtures.TestInputFileBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.scanner.plugin.api.impl.config.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.common.predicates.ArmJsonFilePredicate.ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.ArmJsonFilePredicate.ARM_JSON_FILE_IDENTIFIER_KEY;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_KEY;
import static org.sonar.iac.common.predicates.FilePredicateTestUtils.newInputFileMock;
import static org.sonar.iac.common.testing.IacTestUtils.SQS_HIDDEN_FILES_SUPPORTED_API_VERSION;
import static org.sonar.iac.common.testing.IacTestUtils.SQS_WITHOUT_HIDDEN_FILES_SUPPORT_API_VERSION;

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
    yamlFileTypeResolver.dispatchTimers(durationStatistics());
    predicatesPerType = Map.of(
      FileType.CLOUDFORMATION, yamlFileTypeResolver.cloudFormationFilePredicate,
      FileType.KUBERNETES, yamlFileTypeResolver.kubernetesFilePredicate,
      FileType.HELM, yamlFileTypeResolver.helmFilePredicate,
      FileType.KUSTOMIZE, yamlFileTypeResolver.kustomizationFilePredicate,
      FileType.JVM_CONFIG, yamlFileTypeResolver.jvmConfigFilePredicate,
      FileType.GITHUB_ACTIONS, yamlFileTypeResolver.githubActionsFilePredicate,
      FileType.AZURE_RESOURCE_MANAGER, yamlFileTypeResolver.armJsonFilePredicate);
  }

  // Cloud Formation predicate tests
  @Test
  void shouldMatchCloudFormationPredicate() throws IOException {
    var inputFile = newInputFileMock("file.yaml", VALID_CLOUDFORMATION_CONTENT);
    assertInputFileMatchedOnlyBy(inputFile, FileType.CLOUDFORMATION);
  }

  // Kubernetes predicate tests
  @Test
  void shouldMatchKubernetesPredicateRegardlessOfFileName() throws IOException {
    var inputFile = newInputFileMock("file.yaml", VALID_KUBERNETES_CONTENT, "kubernetes", InputFile.Type.MAIN);
    assertInputFileMatchedOnlyBy(inputFile, FileType.KUBERNETES);
  }

  @Test
  void shouldMatchKubernetesPredicateOverJvmConfigPredicate() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings());
    var inputFile = IacTestUtils.inputFile("src/main/resources/vars/application-prod.yaml", tempDir, VALID_KUBERNETES_CONTENT, "kubernetes");
    sensorContext.fileSystem().add(inputFile);

    assertFileResolvesTo(sensorContext, inputFile, FileType.KUBERNETES);
  }

  @Test
  void shouldMatchKubernetesPredicateOverCloudFormationPredicate() {
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings);
    var inputFile = IacTestUtils.inputFile("file.yaml", tempDir, "%s%n%s".formatted(VALID_KUBERNETES_CONTENT, VALID_CLOUDFORMATION_CONTENT), "kubernetes");
    sensorContext.fileSystem().add(inputFile);

    assertFileResolvesTo(sensorContext, inputFile, FileType.KUBERNETES);
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
  void shouldMatchKustomizationPredicateOverKubernetesPredicate() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings());
    var inputFile = IacTestUtils.inputFile("kustomization.yaml", tempDir, VALID_KUBERNETES_CONTENT, "kubernetes");
    sensorContext.fileSystem().add(inputFile);

    assertFileResolvesTo(sensorContext, inputFile, FileType.KUSTOMIZE);
  }

  // Helm predicate tests
  @Test
  void shouldMatchHelmPredicateOverKubernetesPredicate() throws IOException {
    Files.createFile(tempDir.resolve("Chart.yaml"));
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings());
    var inputFile = IacTestUtils.inputFile("templates/pod.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml");
    sensorContext.fileSystem().add(inputFile);

    assertFileResolvesTo(sensorContext, inputFile, FileType.HELM);
  }

  // JVM Config predicate tests
  @Test
  void shouldMatchJvmConfigPredicate() throws IOException {
    var inputFile = newInputFileMock("src/main/resources/vars/application-prod.yaml", "");
    assertInputFileMatchedOnlyBy(inputFile, FileType.JVM_CONFIG);
  }

  @Test
  void shouldMatchCloudFormationPredicateOverJvmConfigPredicate() {
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings);
    var inputFile = IacTestUtils.inputFile("src/main/resources/vars/application-prod.yaml", tempDir, VALID_CLOUDFORMATION_CONTENT, "yaml");
    sensorContext.fileSystem().add(inputFile);

    assertFileResolvesTo(sensorContext, inputFile, FileType.CLOUDFORMATION);
  }

  // Github Actions predicate tests
  @Test
  void shouldMatchGithubActionsPredicate() throws IOException {
    var inputFile = newInputFileMock(".github/workflows/deploy.yaml", "");
    assertInputFileMatchedOnlyBy(inputFile, FileType.GITHUB_ACTIONS);
  }

  @Test
  void shouldMatchGithubActionsPredicateOverKubernetesPredicate() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings());
    var inputFile = IacTestUtils.inputFile(".github/workflows/deploy.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml");
    sensorContext.fileSystem().add(inputFile);

    assertFileResolvesTo(sensorContext, inputFile, FileType.GITHUB_ACTIONS);
  }

  // Hidden file predicate tests
  @Test
  void shouldClassifyHiddenGithubActionsWorkflowFileViaHiddenFilePredicate() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings()).setRuntime(SQS_HIDDEN_FILES_SUPPORTED_API_VERSION);
    var inputFile = IacTestUtils.inputFile(".github/workflows/deploy.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml", true);
    sensorContext.fileSystem().add(inputFile);

    assertFileResolvesTo(sensorContext, inputFile, FileType.GITHUB_ACTIONS);
  }

  @Test
  void shouldNotClassifyHiddenNonWorkflowFileWhenHiddenFilesAnalysisIsSupported() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings()).setRuntime(SQS_HIDDEN_FILES_SUPPORTED_API_VERSION);
    var inputFile = IacTestUtils.inputFile(".hidden/deployment.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml", true);
    sensorContext.fileSystem().add(inputFile);
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());

    resolver.classifyInputFiles(sensorContext, durationStatistics());

    for (var type : FileType.values()) {
      assertThat(resolver.getInputFiles(type)).doesNotContain(inputFile);
    }
  }

  @Test
  void shouldClassifyHiddenFileNormallyWhenHiddenFilesAnalysisIsNotSupported() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings()).setRuntime(SQS_WITHOUT_HIDDEN_FILES_SUPPORT_API_VERSION);
    var inputFile = IacTestUtils.inputFile(".hidden/deployment.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml", true);
    sensorContext.fileSystem().add(inputFile);

    assertFileResolvesTo(sensorContext, inputFile, FileType.KUBERNETES);
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
    var content = "$schema: https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#";
    var inputFile = newInputFileMock("template.yaml", content, "yaml", InputFile.Type.MAIN);
    predicatesPerType.values().forEach(predicate -> assertThat(predicate.apply(inputFile)).isFalse());
  }

  @Test
  void shouldNotReadContentOfFileWithNonCandidateLanguage() throws IOException {
    var inputFile = newInputFileMock("image.gif", "not relevant", "gif", InputFile.Type.MAIN);

    predicatesPerType.values().forEach(predicate -> assertThat(predicate.apply(inputFile)).isFalse());

    verify(inputFile, never()).inputStream();
    verify(inputFile, never()).charset();
  }

  @Test
  void shouldStoreInCacheOnlyFilesResolvedByThePredicateScan() {
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings);
    var jvmConfigFile = IacTestUtils.inputFile("src/main/resources/vars/application-prod.yaml", tempDir, "", "yaml");
    var plainFile = IacTestUtils.inputFile("some/plain.yaml", tempDir, "key: value", "yaml");
    sensorContext.fileSystem().add(jvmConfigFile);
    sensorContext.fileSystem().add(plainFile);
    var cache = new YamlFileTypeCache();
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), cache);

    resolver.classifyInputFiles(sensorContext, durationStatistics());

    assertThat(cache.hasKnownType(jvmConfigFile)).isTrue();
    assertThat(resolver.getInputFiles(FileType.JVM_CONFIG)).containsExactly(jvmConfigFile);
    // plainFile matches no predicate, so it is never recorded in the cache.
    assertThat(cache.hasKnownType(plainFile)).isFalse();
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

    resolver.classifyInputFiles(sensorContext, durationStatistics());

    assertThat(resolver.getInputFiles(FileType.CLOUDFORMATION)).containsExactly(cloudFormationFile);
    assertThat(resolver.getInputFiles(FileType.KUBERNETES)).containsExactly(kubernetesFile);
    assertThat(resolver.getInputFiles(Set.of(FileType.CLOUDFORMATION, FileType.KUBERNETES)))
      .containsExactlyInAnyOrder(cloudFormationFile, kubernetesFile);
  }

  @Test
  void shouldClassifyFilesWhoseLanguageIsASpecializedIacLanguage() {
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings);
    var cloudFormationFile = IacTestUtils.inputFile("template.yaml", tempDir, VALID_CLOUDFORMATION_CONTENT, "cloudformation");
    var kubernetesFile = IacTestUtils.inputFile("deploy.yaml", tempDir, VALID_KUBERNETES_CONTENT, "kubernetes");
    sensorContext.fileSystem().add(cloudFormationFile);
    sensorContext.fileSystem().add(kubernetesFile);
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());

    resolver.classifyInputFiles(sensorContext, durationStatistics());

    assertThat(resolver.getInputFiles(FileType.CLOUDFORMATION)).containsExactly(cloudFormationFile);
    assertThat(resolver.getInputFiles(FileType.KUBERNETES)).containsExactly(kubernetesFile);
  }

  @Test
  void shouldReturnOnlyTheCallingFileSystemsFilesEvenWhenTheCacheIsShared() {
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

    resolverA.classifyInputFiles(moduleA, durationStatistics());
    assertThat(resolverA.getInputFiles(FileType.KUBERNETES)).containsExactly(kubernetesA);

    resolverB.classifyInputFiles(moduleB, durationStatistics());
    assertThat(resolverB.getInputFiles(FileType.KUBERNETES)).containsExactly(kubernetesB);

    // Module B's scan cleared and took over the shared cache, but re-resolving for module A rebinds and rescans it, so
    // querying module A again must still return only module A's file.
    resolverA.classifyInputFiles(moduleA, durationStatistics());
    assertThat(resolverA.getInputFiles(FileType.KUBERNETES)).containsExactly(kubernetesA);
  }

  @Test
  void shouldReturnHelmTplTemplatesWhichHaveNoYamlLanguage() throws IOException {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings());
    Files.createFile(tempDir.resolve("Chart.yaml"));
    var tplFile = IacTestUtils.inputFile("templates/_helpers.tpl", tempDir, "{{- define \"x\" -}}{{- end -}}", null);
    sensorContext.fileSystem().add(tplFile);
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());

    resolver.classifyInputFiles(sensorContext, durationStatistics());

    assertThat(resolver.getInputFiles(FileType.HELM)).containsExactly(tplFile);
  }

  @Test
  void shouldClassifyJvmConfigFileWhoseLanguageIsNoCandidateLanguage() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings());
    var propertiesFile = IacTestUtils.inputFile("src/main/resources/application.properties", tempDir, "key=value", "properties");
    sensorContext.fileSystem().add(propertiesFile);
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());

    resolver.classifyInputFiles(sensorContext, durationStatistics());

    assertThat(resolver.getInputFiles(FileType.JVM_CONFIG)).containsExactly(propertiesFile);
  }

  @Test
  void shouldNotClassifyTestTypeFiles() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings());
    var testTypeFile = new TestInputFileBuilder("moduleKey", "deploy.yaml")
      .setModuleBaseDir(tempDir)
      .setType(InputFile.Type.TEST)
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage("yaml")
      .setContents(VALID_KUBERNETES_CONTENT)
      .build();
    sensorContext.fileSystem().add(testTypeFile);
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());

    resolver.classifyInputFiles(sensorContext, durationStatistics());

    for (var type : FileType.values()) {
      assertThat(resolver.getInputFiles(type)).doesNotContain(testTypeFile);
    }
  }

  @Test
  void shouldNotClassifyHiddenTestTypeGithubActionsWorkflow() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings()).setRuntime(SQS_HIDDEN_FILES_SUPPORTED_API_VERSION);
    var hiddenTestTypeFile = new TestInputFileBuilder("moduleKey", ".github/workflows/deploy.yaml")
      .setModuleBaseDir(tempDir)
      .setType(InputFile.Type.TEST)
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage("yaml")
      .setContents(VALID_KUBERNETES_CONTENT)
      .setHidden(true)
      .build();
    sensorContext.fileSystem().add(hiddenTestTypeFile);
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());

    resolver.classifyInputFiles(sensorContext, durationStatistics());

    for (var type : FileType.values()) {
      assertThat(resolver.getInputFiles(type)).doesNotContain(hiddenTestTypeFile);
    }
  }

  @Test
  void shouldNotClassifyHiddenMainTypeFileWithNonCandidateLanguage() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(new MapSettings()).setRuntime(SQS_HIDDEN_FILES_SUPPORTED_API_VERSION);
    var hiddenNonCandidateLanguageFile = new TestInputFileBuilder("moduleKey", ".github/workflows/deploy.yaml")
      .setModuleBaseDir(tempDir)
      .setType(InputFile.Type.MAIN)
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage("gif")
      .setContents(VALID_KUBERNETES_CONTENT)
      .setHidden(true)
      .build();
    sensorContext.fileSystem().add(hiddenNonCandidateLanguageFile);
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());

    resolver.classifyInputFiles(sensorContext, durationStatistics());

    for (var type : FileType.values()) {
      assertThat(resolver.getInputFiles(type)).doesNotContain(hiddenNonCandidateLanguageFile);
    }
  }

  @Test
  void shouldScanTheFileSystemOnlyOnceWhenClassifyInputFilesIsCalledRepeatedly() {
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings);
    sensorContext.fileSystem().add(IacTestUtils.inputFile("cloudformation.yaml", tempDir, VALID_CLOUDFORMATION_CONTENT, "yaml"));
    sensorContext.fileSystem().add(IacTestUtils.inputFile("kubernetes.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml"));
    var fileSystem = spy(sensorContext.fileSystem());
    var contextSpy = spy(sensorContext);
    doReturn(fileSystem).when(contextSpy).fileSystem();
    var resolver = new YamlFileTypeResolver(fileSystem, sensorContext.config(), new YamlFileTypeCache());

    resolver.classifyInputFiles(contextSpy, durationStatistics());
    resolver.classifyInputFiles(contextSpy, durationStatistics());
    resolver.classifyInputFiles(contextSpy, durationStatistics());

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

  private void assertFileResolvesTo(SensorContextTester sensorContext, InputFile inputFile, FileType expectedType) {
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());
    resolver.classifyInputFiles(sensorContext, durationStatistics());

    assertThat(resolver.getInputFiles(expectedType))
      .describedAs("'%s' file should have been classified as %s".formatted(inputFile, expectedType))
      .contains(inputFile);
    for (var otherType : FileType.values()) {
      if (otherType != expectedType) {
        assertThat(resolver.getInputFiles(otherType))
          .describedAs("'%s' file should NOT have been classified as %s".formatted(inputFile, otherType))
          .doesNotContain(inputFile);
      }
    }
  }
}
