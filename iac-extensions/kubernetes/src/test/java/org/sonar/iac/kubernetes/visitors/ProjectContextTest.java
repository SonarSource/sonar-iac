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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMock;

import java.net.URI;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.ProjectResource;
import org.sonar.iac.kubernetes.model.ServiceAccount;

class ProjectContextTest {
  @TempDir
  private static Path baseDir;

  @Test
  void shouldCorrectlyStoreResourcesAndProvideAccess() {
    var ctx = new ProjectContext();

    var resource11 = mock(ServiceAccount.class);
    ctx.addResource("namespace1", toUri("path1/resource11.yaml").toString(), resource11);
    var resource12 = mock(LimitRange.class);
    ctx.addResource("namespace1", toUri("path1/resource12.yaml").toString(), resource12);
    var resource21 = mock(ServiceAccount.class);
    ctx.addResource("namespace2", toUri("path2/resource21.yaml").toString(), resource21);
    var resource22 = mock(ServiceAccount.class);
    ctx.addResource("namespace2", toUri("path2/resource22.yaml").toString(), resource22);

    assertThat(ctx.getProjectResources("namespace1", toInputFileContext("path1/something.yaml"), ServiceAccount.class)).containsExactly(resource11);
    assertThat(ctx.getProjectResources("namespace1", toInputFileContext("path1/something.yaml"), LimitRange.class)).containsExactly(resource12);
    assertThat(ctx.getProjectResources("namespace1", toInputFileContext("path1/something.yaml"), TestResource.class)).isEmpty();
    assertThat(ctx.getProjectResources("namespace2", toInputFileContext("path2/something.yaml"), ServiceAccount.class)).containsExactlyInAnyOrder(resource21, resource22);
    assertThat(ctx.getProjectResources("namespace1", toInputFileContext("wrong-path/something.yaml"), ServiceAccount.class)).isEmpty();
    assertThat(ctx.getProjectResources("wrong-namespace", toInputFileContext("random-path/something.yaml"), ServiceAccount.class)).isEmpty();
  }

  @Test
  void shouldProvideAccessOnlyToDescendantDirectories() {
    var ctx = new ProjectContext();

    var resource1 = mock(TestResource.class);
    ctx.addResource("default", toUri("path1/resource.yaml").toString(), resource1);
    var resource2 = mock(TestResource.class);
    ctx.addResource("default", toUri("path2/resource.yaml").toString(), resource2);
    var resource3 = mock(TestResource.class);
    ctx.addResource("default", toUri("path1/subdir1/resource.yaml").toString(), resource3);
    var resource4 = mock(TestResource.class);
    ctx.addResource("default", toUri("path1/subdir2/resource.yaml").toString(), resource4);

    assertThat(ctx.getProjectResources("default", toInputFileContext("path1/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource1, resource3, resource4);
    assertThat(ctx.getProjectResources("default", toInputFileContext("path1/subdir1/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource3);
    assertThat(ctx.getProjectResources("default", toInputFileContext("path1/subdir2/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource4);
    assertThat(ctx.getProjectResources("default", toInputFileContext("path1/subdir3/something.yaml"), TestResource.class)).isEmpty();
    assertThat(ctx.getProjectResources("default", toInputFileContext("path2/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource2);
  }

  @Test
  void shouldProvideAccessOnlyToResourcesInTheSameHelmChart() {
    var ctx = new ProjectContext();

    var resource1 = mock(TestResource.class);
    ctx.addResource("default", toUri("path1/templates/resource.yaml").toString(), resource1);
    var resource2 = mock(TestResource.class);
    ctx.addResource("default", toUri("path1/Chart.yaml").toString(), resource2);
    var resource3 = mock(TestResource.class);
    ctx.addResource("default", toUri("path2/resource.yaml").toString(), resource3);

    assertThat(ctx.getProjectResources("default", toHelmInputFileContext("path1/templates/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource1, resource2);
    assertThat(ctx.getProjectResources("default", toHelmInputFileContext("path1/templates/subdir/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource1,
      resource2);
    assertThat(ctx.getProjectResources("default", toHelmInputFileContext("path1/Chart.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource1, resource2);
    assertThat(ctx.getProjectResources("default", toInputFileContext("path2/something.yaml"), TestResource.class)).containsExactly(resource3);
    assertThat(ctx.getProjectResources("default", toInputFileContext("path2/subdir/something.yaml"), TestResource.class)).isEmpty();
  }

  private static URI toUri(String path) {
    return baseDir.resolve(path).normalize().toUri();
  }

  private static InputFileContext toInputFileContext(String path) {
    var inputFileContext = createInputFileContextMock(path);
    when(inputFileContext.inputFile.uri()).thenReturn(baseDir.resolve(path).toUri());
    return inputFileContext;
  }

  private static HelmInputFileContext toHelmInputFileContext(String path) {
    var inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(baseDir.resolve(path).toUri());
    try (var ignored = mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(baseDir.resolve("path1"));
      return new HelmInputFileContext(mock(SensorContext.class), inputFile);
    }
  }

  private static class TestResource implements ProjectResource {
  }
}
