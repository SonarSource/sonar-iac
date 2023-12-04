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
package org.sonar.iac.common.yaml;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.yaml.tree.FileTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.yaml.YamlTreeUtils.getListValueElements;
import static org.sonar.iac.common.yaml.YamlTreeUtils.scalar;
import static org.sonar.iac.common.yaml.YamlTreeUtils.sequence;

class YamlTreeUtilsTest {

  @Test
  void getListValueElement() {
    assertThat(getListValueElements(scalar(""))).containsExactly("");
    assertThat(getListValueElements(scalar("false"))).containsExactly("false");
    assertThat(getListValueElements(sequence("false", "true", "test"))).containsExactly("false", "true", "test");
    assertThat(getListValueElements(notTextTree())).isEmpty();
    assertThat(getListValueElements(null)).isEmpty();
  }

  private FileTreeImpl notTextTree() {
    return new FileTreeImpl(null, null, null);
  }
}
