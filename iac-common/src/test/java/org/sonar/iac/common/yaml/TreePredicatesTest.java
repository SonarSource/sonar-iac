/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;

class TreePredicatesTest {

  @Test
  void isTrue() {
    assertThat(TreePredicates.isTrue().test(text("true"))).isTrue();
    assertThat(TreePredicates.isTrue().test(text("false"))).isFalse();
    assertThat(TreePredicates.isTrue().test(text(null))).isFalse();
    assertThat(TreePredicates.isTrue().test(notTextTree())).isFalse();
  }

  @Test
  void isEqualTo() {
    assertThat(TreePredicates.isEqualTo("VALUE_TEST").test(text("VALUE_TEST"))).isTrue();
    assertThat(TreePredicates.isEqualTo("VALUE_TEST").test(text("NOT_VALUE_TEST"))).isFalse();
    assertThat(TreePredicates.isEqualTo("VALUE_TEST").test(text(null))).isFalse();
    assertThat(TreePredicates.isEqualTo("VALUE_TEST").test(notTextTree())).isFalse();
  }

  @Test
  void isEqualTo() {
    assertThat(TreePredicates.isEqualTo("VALUE_TEST").test(text("VALUE_TEST"))).isTrue();
  }

  private ScalarTree text(String value) {
    return new ScalarTreeImpl(value, null, null);
  }

  private FileTreeImpl notTextTree() {
    return new FileTreeImpl(null, null);
  }
}
