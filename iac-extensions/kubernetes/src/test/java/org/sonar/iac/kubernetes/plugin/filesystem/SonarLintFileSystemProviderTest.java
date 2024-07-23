/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.kubernetes.plugin.filesystem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SonarLintFileSystemProviderTest {

  private static final Path BASE_DIR = Path.of("src/test/resources/SonarLintFileSystemProvider");

  SonarLintFileSystemProvider provider = new SonarLintFileSystemProvider();

  @Test
  void shouldSetAndGetInputFilesContents() {
    Map<String, String> map = new HashMap<>();
    provider.setInputFilesContents(map);

    var actual = provider.getInputFilesContents();

    assertThat(actual).isSameAs(map);
  }

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
    provider.setInputFilesContents(map);
    var inputFile = IacTestUtils.inputFile("Chart1/templates/service.yaml", BASE_DIR);
    var sensorContext = mock(SensorContext.class);
    FileSystem fileSystem = mock(FileSystem.class);
    when(fileSystem.baseDir()).thenReturn(BASE_DIR.toAbsolutePath().toFile());
    when(sensorContext.fileSystem()).thenReturn(fileSystem);
    var inputFileContext = new HelmInputFileContext(sensorContext, inputFile);

    var actual = provider.inputFilesForHelm(inputFileContext);

    assertThat(actual.keySet()).contains("templates/pod.yaml", "Chart.yaml", "values.yaml");
  }

  private String toUri(String path) {
    return BASE_DIR.resolve(path).toUri().toString();
  }
}
