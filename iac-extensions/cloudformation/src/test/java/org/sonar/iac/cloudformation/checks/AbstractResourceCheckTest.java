/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.common.api.checks.CheckContext;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractResourceCheckTest {

  @Test
  void checkResource_is_called_on_every_resource() {
    Check check = new Check();

    CloudformationVerifier.verifyNoIssue("AbstractResourceCheck/test.yaml", check);

    assertThat(check.foundResources).hasSize(4);
    assertType(check.foundResources.get(0), "type1");
    assertType(check.foundResources.get(1), "type2");
    assertType(check.foundResources.get(2), "type3");
    assertThat(check.foundResources.get(3).type()).isNull();
  }

  private void assertType(AbstractResourceCheck.Resource resource, String type) {
    assertThat(resource.type()).isInstanceOfSatisfying(ScalarTree.class, s -> assertThat(s.value()).isEqualTo(type));
  }

  private static class Check extends AbstractResourceCheck {
    List<Resource> foundResources = new ArrayList<>();

    @Override
    protected void checkResource(CheckContext ctx, Resource resource) {
      foundResources.add(resource);
    }
  }
}
