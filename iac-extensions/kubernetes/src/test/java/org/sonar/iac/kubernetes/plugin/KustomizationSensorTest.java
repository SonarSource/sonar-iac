/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;

class KustomizationSensorTest {

  private static final String KUSTOMIZE_PRESENT = "iac.kustomize";
  private static final String KUSTOMIZE_FILES_COUNT = "iac.kustomize.files.count";
  private static final String KUSTOMIZE_REFERENCED_FILES_COUNT = "iac.kustomize.referenced.files.count";

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private static final Path BASE_DIR = Path.of("src/test/resources");
  private SensorContextTester context;
  private KustomizationInfoProvider kustomizationInfoProvider;
  private KustomizationSensor sensor;

  @BeforeEach
  void setUp() {
    var settings = new MapSettings();
    settings.setProperty(KubernetesSettings.ACTIVATION_KEY, true);
    context = SensorContextTester.create(BASE_DIR);
    context.setSettings(settings);
    kustomizationInfoProvider = new KustomizationInfoProvider();
    sensor = new KustomizationSensor(kustomizationInfoProvider, new KubernetesLanguage());
  }

  @Test
  void shouldDescribeSensor() {
    var descriptor = new DefaultSensorDescriptor();
    sensor.describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("IaC Kustomization Sensor");
    assertThat(descriptor.languages()).containsExactlyInAnyOrder("yaml", "kubernetes");
  }

  @Test
  void shouldSkipProcessWhenSettingDisabled() {
    var settings = new MapSettings();
    settings.setProperty(KubernetesSettings.ACTIVATION_KEY, false);
    context.setSettings(settings);
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - deployment.yaml
        - service.yaml
      """;
    var inputFile = createInputFile("kustomization.yaml", kustomizationContent);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles()).isEmpty();
  }

  @Test
  void shouldProcessKustomizationYamlFile() {
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - deployment.yaml
        - service.yaml
      """;
    var inputFile = createInputFile("kustomization.yaml", kustomizationContent);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles()).hasSize(2);
    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles())
      .containsExactlyInAnyOrder(
        BASE_DIR.toAbsolutePath().resolve("deployment.yaml").normalize().toUri(),
        BASE_DIR.toAbsolutePath().resolve("service.yaml").normalize().toUri());
  }

  @Test
  void shouldProcessKustomizationYmlFile() {
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - config.yaml
      """;
    var inputFile = createInputFile("kustomization.yml", kustomizationContent);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles()).hasSize(1);
    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles())
      .containsExactly(BASE_DIR.toAbsolutePath().resolve("config.yaml").normalize().toUri());
  }

  @ParameterizedTest
  @ValueSource(strings = {"kustomization.yaml", "kustomization.yml", "KUSTOMIZATION.YAML", "Kustomization.Yml"})
  void shouldDetectKustomizationFilesCaseInsensitive(String filename) {
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - test.yaml
      """;
    var inputFile = createInputFile(filename, kustomizationContent);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles()).hasSize(1);
  }

  @Test
  void shouldIgnoreNonKustomizationFiles() {
    var inputFile = createInputFile("deployment.yaml", """
      apiVersion: apps/v1
      kind: Deployment
      metadata:
        name: myapp
      """);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles()).isEmpty();
  }

  @Test
  void shouldProcessMultipleKustomizationFiles() {
    var kustomization1 = createInputFile("base/kustomization.yaml", """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - deployment.yaml
      """);
    var kustomization2 = createInputFile("overlay/kustomization.yaml", """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - ../base
        - service.yaml
      """);
    context.fileSystem().add(kustomization1);
    context.fileSystem().add(kustomization2);

    sensor.execute(context);

    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles()).hasSize(3);
  }

  @Test
  void shouldHandleKustomizationWithPatches() {
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - deployment.yaml
      patches:
        - path: patch.yaml
          target:
            kind: Deployment
      """;
    var inputFile = createInputFile("kustomization.yaml", kustomizationContent);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles()).hasSize(2);
    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles())
      .containsExactlyInAnyOrder(
        BASE_DIR.toAbsolutePath().resolve("deployment.yaml").normalize().toUri(),
        BASE_DIR.toAbsolutePath().resolve("patch.yaml").normalize().toUri());
  }

  @Test
  void shouldHandleKustomizationWithPatchesStrategicMerge() {
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - deployment.yaml
      patchesStrategicMerge:
        - patch1.yaml
        - patch2.yaml
      """;
    var inputFile = createInputFile("kustomization.yaml", kustomizationContent);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles()).hasSize(3);
    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles())
      .containsExactlyInAnyOrder(
        BASE_DIR.toAbsolutePath().resolve("deployment.yaml").normalize().toUri(),
        BASE_DIR.toAbsolutePath().resolve("patch1.yaml").normalize().toUri(),
        BASE_DIR.toAbsolutePath().resolve("patch2.yaml").normalize().toUri());
  }

  @Test
  void shouldHandleEmptyKustomizationFile() {
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      """;
    var inputFile = createInputFile("kustomization.yaml", kustomizationContent);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles()).isEmpty();
  }

  @Test
  void shouldHandleRelativePaths() {
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - ../base/deployment.yaml
        - ./local/service.yaml
      """;
    var inputFile = createInputFile("overlays/kustomization.yaml", kustomizationContent);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles()).hasSize(2);
    assertThat(kustomizationInfoProvider.kustomizationReferencedFiles())
      .containsExactlyInAnyOrder(
        BASE_DIR.toAbsolutePath().resolve("base/deployment.yaml").normalize().toUri(),
        BASE_DIR.toAbsolutePath().resolve("overlays/local/service.yaml").normalize().toUri());
  }

  @Test
  void shouldLogTelemetryWhenProcessingKustomizationFiles() {
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - deployment.yaml
        - service.yaml
      """;
    var inputFile = createInputFile("kustomization.yaml", kustomizationContent);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    var debugLogs = logTester.logs(Level.DEBUG);
    assertThat(debugLogs)
      .anyMatch(log -> log.contains("Kustomization sensor processed 1 kustomization files and collected 2 referenced files"));
  }

  @Test
  void shouldLogTelemetryWhenProcessingMultipleKustomizationFiles() {
    var kustomization1 = createInputFile("base/kustomization.yaml", """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - deployment.yaml
      """);
    var kustomization2 = createInputFile("overlay/kustomization.yaml", """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - service.yaml
        - ingress.yaml
      """);
    context.fileSystem().add(kustomization1);
    context.fileSystem().add(kustomization2);

    sensor.execute(context);

    var debugLogs = logTester.logs(Level.DEBUG);
    assertThat(debugLogs)
      .anyMatch(log -> log.contains("Kustomization sensor processed 2 kustomization files and collected 3 referenced files"));
  }

  @Test
  void shouldLogTelemetryWhenNoKustomizationFiles() {
    sensor.execute(context);

    var debugLogs = logTester.logs(Level.DEBUG);
    assertThat(debugLogs)
      .anyMatch(log -> log.contains("Kustomization sensor processed 0 kustomization files and collected 0 referenced files"));
  }

  @Test
  void shouldAddTelemetryPropertiesWhenProcessingKustomizationFiles() {
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - deployment.yaml
        - service.yaml
      """;
    var inputFile = createInputFile("kustomization.yaml", kustomizationContent);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    assertThat(context.getTelemetryProperties())
      .containsEntry(KUSTOMIZE_PRESENT, "1")
      .containsEntry(KUSTOMIZE_FILES_COUNT, "1")
      .containsEntry(KUSTOMIZE_REFERENCED_FILES_COUNT, "2");
  }

  @Test
  void shouldAddTelemetryPropertiesWhenProcessingMultipleKustomizationFiles() {
    var kustomization1 = createInputFile("base/kustomization.yaml", """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - deployment.yaml
      """);
    var kustomization2 = createInputFile("overlay/kustomization.yaml", """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      resources:
        - service.yaml
        - ingress.yaml
      """);
    context.fileSystem().add(kustomization1);
    context.fileSystem().add(kustomization2);

    sensor.execute(context);

    assertThat(context.getTelemetryProperties())
      .containsEntry(KUSTOMIZE_PRESENT, "1")
      .containsEntry(KUSTOMIZE_FILES_COUNT, "2")
      .containsEntry(KUSTOMIZE_REFERENCED_FILES_COUNT, "3");
  }

  @Test
  void shouldAddTelemetryPropertiesWhenNoKustomizationFiles() {
    sensor.execute(context);

    assertThat(context.getTelemetryProperties())
      .containsEntry(KUSTOMIZE_PRESENT, "0")
      .containsEntry(KUSTOMIZE_FILES_COUNT, "0")
      .containsEntry(KUSTOMIZE_REFERENCED_FILES_COUNT, "0");
  }

  @Test
  void shouldAddTelemetryPropertiesWhenKustomizationFileHasNoResources() {
    var kustomizationContent = """
      apiVersion: kustomize.config.k8s.io/v1beta1
      kind: Kustomization
      """;
    var inputFile = createInputFile("kustomization.yaml", kustomizationContent);
    context.fileSystem().add(inputFile);

    sensor.execute(context);

    assertThat(context.getTelemetryProperties())
      .containsEntry(KUSTOMIZE_PRESENT, "1")
      .containsEntry(KUSTOMIZE_FILES_COUNT, "1")
      .containsEntry(KUSTOMIZE_REFERENCED_FILES_COUNT, "0");
  }

  private org.sonar.api.batch.fs.InputFile createInputFile(String relativePath, String content) {
    return new TestInputFileBuilder("moduleKey", relativePath)
      .setModuleBaseDir(BASE_DIR)
      .setType(org.sonar.api.batch.fs.InputFile.Type.MAIN)
      .setLanguage("yaml")
      .setCharset(StandardCharsets.UTF_8)
      .setContents(content)
      .build();
  }
}
