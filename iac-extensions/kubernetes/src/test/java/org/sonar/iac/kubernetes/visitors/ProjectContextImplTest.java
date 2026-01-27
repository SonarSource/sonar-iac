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
package org.sonar.iac.kubernetes.visitors;

import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.filesystem.FileSystemUtils;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.ProjectResource;
import org.sonar.iac.kubernetes.model.ServiceAccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.filesystem.FileSystemUtils.retrieveHelmProjectFolder;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMock;

class ProjectContextImplTest {
  @TempDir
  private static Path baseDir;

  @Test
  void shouldCorrectlyStoreResourcesAndProvideAccess() {
    var ctx = new ProjectContextImpl();

    var resource11 = mock(ServiceAccount.class);
    ctx.addResource("namespace1", toUri("path1/resource11.yaml").toString(), resource11);
    var resource12 = mock(LimitRange.class);
    ctx.addResource("namespace1", toUri("path1/resource12.yaml").toString(), resource12);
    var resource21 = mock(ServiceAccount.class);
    ctx.addResource("namespace2", toUri("path2/resource21.yaml").toString(), resource21);
    var resource22 = mock(ServiceAccount.class);
    ctx.addResource("namespace2", toUri("path2/resource22.yaml").toString(), resource22);

    assertThat(ctx.getNamespaceProjectResources("namespace1", toInputFileContext("path1/something.yaml"), ServiceAccount.class)).containsExactly(resource11);
    assertThat(ctx.getNamespaceProjectResources("namespace1", toInputFileContext("path1/something.yaml"), LimitRange.class)).containsExactly(resource12);
    assertThat(ctx.getNamespaceProjectResources("namespace1", toInputFileContext("path1/something.yaml"), TestResource.class)).isEmpty();
    assertThat(ctx.getNamespaceProjectResources("namespace2", toInputFileContext("path2/something.yaml"), ServiceAccount.class)).containsExactlyInAnyOrder(resource21,
      resource22);
    assertThat(ctx.getNamespaceProjectResources("namespace1", toInputFileContext("wrong-path/something.yaml"), ServiceAccount.class)).isEmpty();
    assertThat(ctx.getNamespaceProjectResources("wrong-namespace", toInputFileContext("random-path/something.yaml"), ServiceAccount.class)).isEmpty();
  }

  @Test
  void shouldProvideAccessOnlyToDescendantDirectories() {
    var ctx = new ProjectContextImpl();

    var resource1 = mock(TestResource.class);
    ctx.addResource("default", toUri("path1/resource.yaml").toString(), resource1);
    var resource2 = mock(TestResource.class);
    ctx.addResource("default", toUri("path2/resource.yaml").toString(), resource2);
    var resource3 = mock(TestResource.class);
    ctx.addResource("default", toUri("path1/subdir1/resource.yaml").toString(), resource3);
    var resource4 = mock(TestResource.class);
    ctx.addResource("default", toUri("path1/subdir2/resource.yaml").toString(), resource4);

    assertThat(ctx.getNamespaceProjectResources("default", toInputFileContext("path1/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource1, resource3,
      resource4);
    assertThat(ctx.getNamespaceProjectResources("default", toInputFileContext("path1/subdir1/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource3);
    assertThat(ctx.getNamespaceProjectResources("default", toInputFileContext("path1/subdir2/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource4);
    assertThat(ctx.getNamespaceProjectResources("default", toInputFileContext("path1/subdir3/something.yaml"), TestResource.class)).isEmpty();
    assertThat(ctx.getNamespaceProjectResources("default", toInputFileContext("path2/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource2);
  }

  @Test
  void shouldProvideAccessOnlyToResourcesInTheSameHelmChart() {
    var ctx = new ProjectContextImpl();

    var resource1 = mock(TestResource.class);
    ctx.addResource("default", toUri("path1/templates/resource.yaml").toString(), resource1);
    var resource2 = mock(TestResource.class);
    ctx.addResource("default", toUri("path1/Chart.yaml").toString(), resource2);
    var resource3 = mock(TestResource.class);
    ctx.addResource("default", toUri("path2/resource.yaml").toString(), resource3);

    assertThat(ctx.getNamespaceProjectResources("default", toHelmInputFileContext("path1/templates/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource1,
      resource2);
    assertThat(ctx.getNamespaceProjectResources("default", toHelmInputFileContext("path1/templates/subdir/something.yaml"), TestResource.class)).containsExactlyInAnyOrder(
      resource1,
      resource2);
    assertThat(ctx.getNamespaceProjectResources("default", toHelmInputFileContext("path1/Chart.yaml"), TestResource.class)).containsExactlyInAnyOrder(resource1, resource2);
    assertThat(ctx.getNamespaceProjectResources("default", toInputFileContext("path2/something.yaml"), TestResource.class)).containsExactly(resource3);
    assertThat(ctx.getNamespaceProjectResources("default", toInputFileContext("path2/subdir/something.yaml"), TestResource.class)).isEmpty();
  }

  @Test
  void shouldRemoveResource() {
    var ctx = new ProjectContextImpl();

    var resource1 = mock(TestResource.class);
    var uri = toUri("path1/templates/resource.yaml").toString();
    ctx.addResource("default", uri, resource1);
    ctx.removeResource(uri);

    assertThat(ctx.getNamespaceProjectResources("default", toHelmInputFileContext("path1/templates/resource.yaml"), TestResource.class)).isEmpty();
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
    try (var ignored = mockStatic(FileSystemUtils.class)) {
      when(retrieveHelmProjectFolder(any(), any())).thenReturn(baseDir.resolve("path1"));
      return new HelmInputFileContext(mock(SensorContext.class), inputFile, null);
    }
  }

  @Test
  void shouldTrackKustomizationReferencedFiles() {
    var ctx = new ProjectContextImpl();

    // Set kustomization-referenced files
    var file1 = baseDir.resolve("overlays/dev/deployment.yaml").normalize();
    var file2 = baseDir.resolve("overlays/dev/service.yaml").normalize();
    var file3 = baseDir.resolve("base/deployment.yaml").normalize();

    ctx.setKustomizationReferencedFiles(Set.of(file1, file2, file3));

    // Create input file contexts with matching paths
    var inputFileContext1 = toInputFileContext("overlays/dev/deployment.yaml");
    var inputFileContext2 = toInputFileContext("overlays/dev/service.yaml");
    var inputFileContext3 = toInputFileContext("base/deployment.yaml");
    var inputFileContext4 = toInputFileContext("base/service.yaml"); // Not in kustomization

    // Verify isKustomizationResource works correctly
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext1)).isTrue();
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext2)).isTrue();
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext3)).isTrue();
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext4)).isFalse();
  }

  @Test
  void shouldHandleEmptyKustomizationReferencedFiles() {
    var ctx = new ProjectContextImpl();

    var inputFileContext = toInputFileContext("some/file.yaml");

    // No files added, should return false
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext)).isFalse();
  }

  @Test
  void shouldNormalizePathsWhenCheckingKustomizationResource() {
    var ctx = new ProjectContextImpl();

    // Set a normalized path
    var normalizedPath = baseDir.resolve("overlays/dev/deployment.yaml").normalize();
    ctx.setKustomizationReferencedFiles(Set.of(normalizedPath));

    // Create context with path that needs normalization
    var inputFileContext = toInputFileContext("overlays/dev/./deployment.yaml");

    // Should still match due to normalization
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext)).isTrue();
  }

  @Test
  void shouldReplaceKustomizationReferencedFilesWhenCalledMultipleTimes() {
    var ctx = new ProjectContextImpl();

    // Set initial files
    var file1 = baseDir.resolve("overlays/dev/deployment.yaml").normalize();
    var file2 = baseDir.resolve("overlays/dev/service.yaml").normalize();
    ctx.setKustomizationReferencedFiles(Set.of(file1, file2));

    var inputFileContext1 = toInputFileContext("overlays/dev/deployment.yaml");
    var inputFileContext2 = toInputFileContext("overlays/dev/service.yaml");
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext1)).isTrue();
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext2)).isTrue();

    // Replace with new files
    var file3 = baseDir.resolve("base/deployment.yaml").normalize();
    ctx.setKustomizationReferencedFiles(Set.of(file3));

    var inputFileContext3 = toInputFileContext("base/deployment.yaml");
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext1)).isFalse(); // Should be removed
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext2)).isFalse(); // Should be removed
    assertThat(ctx.isKustomizationReferencedFile(inputFileContext3)).isTrue(); // Should be added
  }

  private static class TestResource implements ProjectResource {
  }
}
