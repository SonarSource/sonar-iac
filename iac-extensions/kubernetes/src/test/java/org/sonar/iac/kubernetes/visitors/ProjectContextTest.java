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

    var resource1 = mock(ServiceAccount.class);
    builder.addResource("namespace1", "path1", resource1);
    var resource2 = mock(LimitRange.class);
    builder.addResource("namespace1", "path1", resource2);
    var resource3 = mock(ServiceAccount.class);
    builder.addResource("namespace2", "path2", resource3);
    var resource4 = mock(ServiceAccount.class);
    builder.addResource("namespace2", "path2", resource4);
    var ctx = builder.build();

    assertThat(ctx.getProjectResource("namespace1", "path1", ServiceAccount.class)).containsExactly(resource1);
    assertThat(ctx.getProjectResource("namespace1", "path1", LimitRange.class)).containsExactly(resource2);
    assertThat(ctx.getProjectResource("namespace1", "path1", TestResource.class)).isEmpty();
    assertThat(ctx.getProjectResource("namespace2", "path2", ServiceAccount.class)).containsExactlyInAnyOrder(resource3, resource4);
    assertThat(ctx.getProjectResource("namespace1", "wrong-path", ServiceAccount.class)).isEmpty();
    assertThat(ctx.getProjectResource("wrong-namespace", "random-path", ServiceAccount.class)).isEmpty();
  }

  private static class TestResource implements ProjectResource {
  }
}
