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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.yaml.YamlParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KustomizationParserTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @TempDir
  static Path baseDir;

  @ParameterizedTest
  @MethodSource("provideExtractionTestCases")
  void shouldExtractAndResolveReferencedFiles(String code, String[] expectedPathsFromBaseDir) throws IOException {
    // Create sensor context and input file
    var context = SensorContextTester.create(baseDir);

    var kustomizationPath = Path.of("overlays", "dev", "kustomization.yaml")
      .toString()
      .replace("\\", "/");

    var inputFile = new TestInputFileBuilder("moduleKey", kustomizationPath)
      .setModuleBaseDir(baseDir)
      .setType(org.sonar.api.batch.fs.InputFile.Type.MAIN)
      .setLanguage("yaml")
      .setCharset(StandardCharsets.UTF_8)
      .setContents(code)
      .build();

    var parser = new KustomizationParser(new YamlParser());

    var result = parser.parse(context, inputFile);

    // Convert expected paths (relative to baseDir) to absolute paths
    var expectedAbsolutePaths = new Path[expectedPathsFromBaseDir.length];
    for (int i = 0; i < expectedPathsFromBaseDir.length; i++) {
      expectedAbsolutePaths[i] = baseDir.toRealPath(LinkOption.NOFOLLOW_LINKS).resolve(expectedPathsFromBaseDir[i]).normalize();
    }

    if (expectedAbsolutePaths.length == 0) {
      assertThat(result).isEmpty();
    } else {
      assertThat(result)
        .hasSize(expectedAbsolutePaths.length)
        .containsExactlyInAnyOrder(expectedAbsolutePaths);
    }
  }

  private static Stream<Arguments> provideExtractionTestCases() {
    return Stream.of(
      // Basic extraction tests
      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          resources:
            - deployment.yaml
            - service.yaml
            - namespace.yaml
          """,
        new String[] {"overlays/dev/deployment.yaml", "overlays/dev/service.yaml", "overlays/dev/namespace.yaml"}),

      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          patches:
            - path: patch-deployment.yaml
              target:
                kind: Deployment
            - path: patch-service.yaml
          """,
        new String[] {"overlays/dev/patch-deployment.yaml", "overlays/dev/patch-service.yaml"}),

      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          patchesStrategicMerge:
            - patch1.yaml
            - patch2.yaml
          """,
        new String[] {"overlays/dev/patch1.yaml", "overlays/dev/patch2.yaml"}),

      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          patchesJson6902:
            - path: json-patch.yaml
              target:
                kind: Deployment
                name: my-app
          """,
        new String[] {"overlays/dev/json-patch.yaml"}),

      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          resources:
            - deployment.yaml
            - service.yaml
          patches:
            - path: patch1.yaml
          patchesStrategicMerge:
            - patch2.yaml
          patchesJson6902:
            - path: patch3.yaml
              target:
                kind: Deployment
          """,
        new String[] {"overlays/dev/deployment.yaml", "overlays/dev/service.yaml", "overlays/dev/patch1.yaml", "overlays/dev/patch2.yaml", "overlays/dev/patch3.yaml"}),

      // Path handling tests - relative paths are resolved
      // Kustomization is at: baseDir/overlays/dev/kustomization.yaml
      // So ../../base -> baseDir/base, ../prod -> baseDir/overlays/prod, ./local.yaml -> baseDir/overlays/dev/local.yaml
      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          resources:
            - ../../base/deployment.yaml
            - ../prod/config.yaml
            - ./local.yaml
          """,
        new String[] {"base/deployment.yaml", "overlays/prod/config.yaml", "overlays/dev/local.yaml"}),

      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          resources:
            - deployment.yaml
            - https://raw.githubusercontent.com/example/repo/main/resource.yaml
            - http://example.com/config.yaml
          """,
        new String[] {"overlays/dev/deployment.yaml"}),

      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          patches:
            - path: patch-file.yaml
            - patch: |-
                apiVersion: apps/v1
                kind: Deployment
                metadata:
                  name: my-app
          """,
        new String[] {"overlays/dev/patch-file.yaml"}),

      // Edge cases
      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          """,
        new String[] {}),

      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          namespace: my-namespace
          namePrefix: app-
          """,
        new String[] {}),

      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          resources: []
          patches: []
          """,
        new String[] {}),

      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          resources:
            - deployment.yaml
          ---
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization
          resources:
            - service.yaml
          """,
        new String[] {"overlays/dev/deployment.yaml", "overlays/dev/service.yaml"}),

      // Complex real-world case
      Arguments.of(
        """
          apiVersion: kustomize.config.k8s.io/v1beta1
          kind: Kustomization

          namespace: production

          resources:
            - ../../base/base.yaml
            - deployment.yaml
            - service.yaml
            - ingress.yaml

          patches:
            - path: patches/deployment-prod.yaml
              target:
                kind: Deployment
                name: myapp
            - path: patches/service-prod.yaml

          patchesStrategicMerge:
            - patches/strategic-merge.yaml

          patchesJson6902:
            - path: patches/json-patch.yaml
              target:
                group: apps
                version: v1
                kind: Deployment
                name: myapp

          configMapGenerator:
            - name: app-config
              files:
                - config.properties
          """,
        new String[] {
          "base/base.yaml",
          "overlays/dev/deployment.yaml",
          "overlays/dev/service.yaml",
          "overlays/dev/ingress.yaml",
          "overlays/dev/patches/deployment-prod.yaml",
          "overlays/dev/patches/service-prod.yaml",
          "overlays/dev/patches/strategic-merge.yaml",
          "overlays/dev/patches/json-patch.yaml"
        }));
  }

  @Test
  void shouldLogAndReturnEmptySetOnIOException() throws IOException {
    var context = SensorContextTester.create(baseDir);
    var kustomizationPath = baseDir.resolve("kustomization.yaml");

    var inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(kustomizationPath.toUri());
    when(inputFile.toString()).thenReturn("kustomization.yaml");
    when(inputFile.contents()).thenThrow(new IOException("Test IO error"));

    var parser = new KustomizationParser(new YamlParser());

    var result = parser.parse(context, inputFile);

    assertThat(result).isEmpty();
    var debugLogs = logTester.logs(Level.DEBUG);
    assertThat(debugLogs)
      .anyMatch(log -> log.contains("Failed to parse kustomization file") && log.contains("Test IO error"));
  }
}
