/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.kubernetes.plugin.filesystem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.kubernetes.plugin.SonarLintFileListener;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SonarLintFileSystemProviderTest {

  private static final Path BASE_DIR = Path.of("src/test/resources/SonarLintFileSystemProvider");

  @Test
  void shouldFilterFilesByHelmProjectDirectoryAndExcludeInputFile() {
    Map<String, String> map = new HashMap<>();
    map.put(toUri("Chart1/Chart.yaml"), "name: dummy");
    map.put(toUri("Chart1/values.yaml"), "key: value");
    map.put(toUri("Chart1/templates/service.yaml"), "kind: Service");
    map.put(toUri("Chart1/templates/pod.yaml"), "kind: Service");
    map.put(toUri("Chart2/Chart.yaml"), "name: dummy");
    map.put(toUri("Chart2/values.yaml"), "key: value");
    map.put(toUri("Chart2/templates/service.yaml"), "kind: Service");
    map.put(toUri("Chart2/templates/pod.yaml"), "kind: Service");
    var sonarLintFileListener = mock(SonarLintFileListener.class);
    when(sonarLintFileListener.inputFilesContents()).thenReturn(map);
    var provider = new SonarLintFileSystemProvider(sonarLintFileListener);
    var inputFile = IacTestUtils.inputFile("Chart1/templates/service.yaml", BASE_DIR);
    var sensorContext = mock(SensorContext.class);
    FileSystem fileSystem = mock(FileSystem.class);
    when(fileSystem.baseDir()).thenReturn(BASE_DIR.toAbsolutePath().toFile());
    when(sensorContext.fileSystem()).thenReturn(fileSystem);
    var inputFileContext = new HelmInputFileContext(sensorContext, inputFile, null);

    var actual = provider.inputFilesForHelm(inputFileContext);

    assertThat(actual).containsOnlyKeys("templates/pod.yaml", "Chart.yaml", "values.yaml");
  }

  @Test
  void shouldNotFailWhenHelmProjectDirectoryIsNull() {
    var inputFileContext = mock(HelmInputFileContext.class);
    var sonarLintFileListener = mock(SonarLintFileListener.class);
    var provider = new SonarLintFileSystemProvider(sonarLintFileListener);

    var actual = provider.inputFilesForHelm(inputFileContext);
    assertThat(actual).isEmpty();
  }

  private String toUri(String path) {
    return BASE_DIR.resolve(path).toUri().toString();
  }
}
