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
package org.sonar.iac.kubernetes.visitors;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.ServiceAccount;
import org.sonar.iac.kubernetes.plugin.HelmProcessor;
import org.sonar.iac.kubernetes.plugin.KubernetesParser;
import org.sonar.iac.kubernetes.plugin.KubernetesParserStatistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectContextEnricherVisitorTest {
  private static final TreeParser<FileTree> PARSER = new KubernetesParser(
    mock(HelmProcessor.class),
    new KubernetesParserStatistics());
  @TempDir
  private static Path BASE_DIR;
  private static SensorContextTester sensorContext;

  @BeforeAll
  static void setUp() {
    sensorContext = SensorContextTester.create(BASE_DIR);
  }

  @Test
  void shouldStoreProjectResources() {
    var code = """
      apiVersion: v1
      kind: ServiceAccount
      metadata:
        name: my-service-account
        namespace: my-namespace
      automountServiceAccountToken: true
      ---
      apiVersion: v1
      kind: Pod
      metadata:
        name: my-pod
        namespace: my-namespace
      spec:
        serviceAccountName: my-service-account
      """;
    var inputFileContext = IacTestUtils.createInputFileContextMock("test.yaml");
    when(inputFileContext.inputFile.uri()).thenReturn(BASE_DIR.resolve("dir1/dir2/test.yaml").toUri());
    var tree = PARSER.parse(code, inputFileContext);
    var projectContextBuilder = spy(new ProjectContext.Builder(sensorContext.fileSystem()));
    var visitor = new ProjectContextEnricherVisitor(projectContextBuilder);

    visitor.scan(inputFileContext, tree);

    var projectContext = projectContextBuilder.build();
    verify(projectContextBuilder, times(1)).addResource(anyString(), any(), any());
    assertThat(projectContext.getProjectResources("my-namespace", BASE_DIR.resolve("dir1/dir2/test.yaml").toString(), ServiceAccount.class)).isNotEmpty();
    assertThat(projectContext.getProjectResources("my-namespace", BASE_DIR.resolve("dir1/dir2/test.yaml").toString(), LimitRange.class)).isEmpty();
  }
}
