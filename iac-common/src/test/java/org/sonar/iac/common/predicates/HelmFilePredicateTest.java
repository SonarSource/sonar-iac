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
package org.sonar.iac.common.predicates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class HelmFilePredicateTest {

  @TempDir
  private Path tempDir;
  private SensorContext context;
  private static final String POD_SPEC = """
    apiVersion: v1
    kind: Pod
    metadata:
      name: my-pod
    """;

  @BeforeEach
  void setUp() {
    context = SensorContextTester.create(tempDir);
  }

  @ParameterizedTest
  @ValueSource(strings = {"templates/my-pod.yaml", "values.yaml"})
  void shouldMatchHelmYamlFile(String path) throws IOException {
    var predicate = new HelmFilePredicate(context);
    Files.createFile(tempDir.resolve("Chart.yaml"));

    var matches = predicate.apply(IacTestUtils.inputFile(path, tempDir, "", "yaml"));

    assertThat(matches).isTrue();
  }

  @Test
  void shouldMatchHelmTplFile() throws IOException {
    var predicate = new HelmFilePredicate(context);
    Files.createFile(tempDir.resolve("Chart.yaml"));

    var matches = predicate.apply(IacTestUtils.inputFile("templates/utils.tpl", tempDir, "", ""));

    assertThat(matches).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"yaml", "kubernetes"})
  void shouldNotMatchKubernetesFileWithLanguage(String language) {
    var predicate = new HelmFilePredicate(context);

    var matches = predicate.apply(IacTestUtils.inputFile("test.yaml", tempDir, POD_SPEC, language));

    assertThat(matches).isFalse();
  }
}
