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
package org.sonar.iac.kubernetes.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;

class MemoryLimitCheckTest {
  IacCheck check = new MemoryLimitCheck();

  @Test
  void podObjectWithEmptyMemory() {
    KubernetesVerifier.verify("MemoryLimitCheck/pod_object_memory_empty.yaml", check);
  }

  @Test
  void podObjectWithMissingAttributesAndBlocks() {
    KubernetesVerifier.verify("MemoryLimitCheck/pod_object_missing_fields.yaml", check);
  }

  @Test
  void templateObjectWithEmptyMemory() {
    KubernetesVerifier.verify("MemoryLimitCheck/template_object_memory_empty.yaml", check);
  }

  @Test
  void templateObjectWithMissingAttributesAndBlocks() {
    KubernetesVerifier.verify("MemoryLimitCheck/template_object.missing_fields.yaml", check);
  }
}
