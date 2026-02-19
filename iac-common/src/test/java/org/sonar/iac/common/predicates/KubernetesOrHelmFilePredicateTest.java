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
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_KEY;

class KubernetesOrHelmFilePredicateTest {
  @TempDir
  private Path tempDir;
  private SensorContext context;
  private DurationStatistics.Timer timer;
  private static final String POD_SPEC = """
    apiVersion: v1
    kind: Pod
    metadata:
      name: my-pod
    """;

  @BeforeEach
  void setUp() {
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE);
    context = SensorContextTester.create(tempDir).setSettings(settings);
    timer = mock(DurationStatistics.Timer.class);
    when(timer.time(any())).thenAnswer(invocation -> invocation.getArgument(0, Supplier.class).get());
  }

  @ParameterizedTest
  @ValueSource(strings = {"yaml", "kubernetes"})
  void shouldMatchKubernetesFileWithLanguage(String language) {
    var predicate = new KubernetesOrHelmFilePredicate(context, false, timer);

    var matches = predicate.apply(IacTestUtils.inputFile("test.yaml", tempDir, POD_SPEC, language));

    assertThat(matches).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"templates/my-pod.yaml", "values.yaml"})
  void shouldMatchHelmYamlFile(String path) throws IOException {
    var predicate = new KubernetesOrHelmFilePredicate(context, false, timer);
    Files.createFile(tempDir.resolve("Chart.yaml"));

    var matches = predicate.apply(IacTestUtils.inputFile(path, tempDir, POD_SPEC, "yaml"));

    assertThat(matches).isTrue();
  }

  @Test
  void shouldMatchHelmTplFile() throws IOException {
    var predicate = new KubernetesOrHelmFilePredicate(context, false, timer);
    Files.createFile(tempDir.resolve("Chart.yaml"));

    var matches = predicate.apply(IacTestUtils.inputFile("templates/utils.tpl", tempDir, POD_SPEC, ""));

    assertThat(matches).isTrue();
  }

  @Test
  void shouldNotMatchNonYamlFileWithKubernetesContent() {
    var predicate = new KubernetesOrHelmFilePredicate(context, false, timer);

    var matches = predicate.apply(IacTestUtils.inputFile("test.txt", tempDir, POD_SPEC, "text"));

    assertThat(matches).isFalse();
  }
}
