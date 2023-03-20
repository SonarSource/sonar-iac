/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.common.checks;

import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTextTree.text;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTree.tree;

class ResourceAccessPolicyVectorTest {

  @Test
  void loadJsonFile() {
    assertThrows(IOException.class, () -> ResourceAccessPolicyVector.loadJsonFile("unknown.json"),
      "No able to load unknown.json");
  }

  @Test
  void isResourceAccessPolicy() {
    assertThat(ResourceAccessPolicyVector.isResourceAccessPolicy(text("backup-gateway:Backup"))).isTrue();
    assertThat(ResourceAccessPolicyVector.isResourceAccessPolicy(text("foo:bar"))).isFalse();
    assertThat(ResourceAccessPolicyVector.isResourceAccessPolicy(tree())).isFalse();
  }
}
