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

import org.junit.jupiter.api.Test;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.ProjectResource;
import org.sonar.iac.kubernetes.model.ServiceAccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ProjectContextTest {

  @Test
  void shouldCorrectlyStoreResourcesAndProvideAccess() {
    var builder = ProjectContext.builder();

    var resource11 = mock(ServiceAccount.class);
    builder.addResource("namespace1", "path1/resource11.yaml", resource11);
    var resource12 = mock(LimitRange.class);
    builder.addResource("namespace1", "path1/resource12.yaml", resource12);
    var resource21 = mock(ServiceAccount.class);
    builder.addResource("namespace2", "path2/resource21.yaml", resource21);
    var resource22 = mock(ServiceAccount.class);
    builder.addResource("namespace2", "path2/resource22.yaml", resource22);
    var ctx = builder.build();

    assertThat(ctx.getProjectResources("namespace1", "path1/something.yaml", ServiceAccount.class)).containsExactly(resource11);
    assertThat(ctx.getProjectResources("namespace1", "path1/something.yaml", LimitRange.class)).containsExactly(resource12);
    assertThat(ctx.getProjectResources("namespace1", "path1/something.yaml", TestResource.class)).isEmpty();
    assertThat(ctx.getProjectResources("namespace2", "path2/something.yaml", ServiceAccount.class)).containsExactlyInAnyOrder(resource21, resource22);
    assertThat(ctx.getProjectResources("namespace1", "wrong-path/something.yaml", ServiceAccount.class)).isEmpty();
    assertThat(ctx.getProjectResources("wrong-namespace", "random-path/something.yaml", ServiceAccount.class)).isEmpty();
  }

  @Test
  void shouldProvideAccessOnlyToDescendantDirectories() {
    var builder = ProjectContext.builder();

    var resource1 = mock(TestResource.class);
    builder.addResource("default", "path1/resource.yaml", resource1);
    var resource2 = mock(TestResource.class);
    builder.addResource("default", "path2/resource.yaml", resource2);
    var resource3 = mock(TestResource.class);
    builder.addResource("default", "path1/subdir1/resource.yaml", resource3);
    var resource4 = mock(TestResource.class);
    builder.addResource("default", "path1/subdir2/resource.yaml", resource4);
    var ctx = builder.build();

    assertThat(ctx.getProjectResources("default", "path1/something.yaml", TestResource.class)).containsExactlyInAnyOrder(resource1, resource3, resource4);
    assertThat(ctx.getProjectResources("default", "path1/subdir1/something.yaml", TestResource.class)).containsExactlyInAnyOrder(resource3);
    assertThat(ctx.getProjectResources("default", "path1/subdir2/something.yaml", TestResource.class)).containsExactlyInAnyOrder(resource4);
    assertThat(ctx.getProjectResources("default", "path1/subdir3/something.yaml", TestResource.class)).isEmpty();
    assertThat(ctx.getProjectResources("default", "path2/something.yaml", TestResource.class)).containsExactlyInAnyOrder(resource2);
  }

  private static class TestResource implements ProjectResource {
  }
}
