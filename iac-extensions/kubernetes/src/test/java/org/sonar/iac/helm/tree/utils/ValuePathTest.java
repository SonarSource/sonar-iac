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
package org.sonar.iac.helm.tree.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValuePathTest {

  @Test
  void shouldReturnPath() {
    var valuePath = new ValuePath("foo", "bar");
    assertThat(valuePath.path()).contains("foo", "bar");
  }

  @Test
  void shouldVerifyEqualsAndHashCode() {
    var valuePath1 = new ValuePath("foo", "bar");
    var valuePath2 = new ValuePath("foo", "bar");
    var valuePath3 = new ValuePath("aaa", "bbb");

    assertThat(valuePath1)
      .isEqualTo(valuePath1)
      .hasSameHashCodeAs(valuePath1)
      .isEqualTo(valuePath2)
      .hasSameHashCodeAs(valuePath2.hashCode())
      .isNotEqualTo(valuePath3)
      .doesNotHaveSameHashCodeAs(valuePath3)
      .isNotEqualTo("dummy")
      .isNotEqualTo(null);
  }

  @Test
  void shouldVerifyToString() {
    var valuePath = new ValuePath("foo", "bar");
    assertThat(valuePath).hasToString("ValuePath{path=[foo, bar]}");
  }
}
