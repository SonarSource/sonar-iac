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
package org.sonar.iac.common.checks;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrivilegeEscalationVectorTest {

  @Test
  void getWildcardPermutations_with_different_prefix() {
    PrivilegeEscalationVector vector = new PrivilegeEscalationVector("foo:bar", "bar:foo");
    Set<PrivilegeEscalationVector> permutations = vector.getWildcardPermutations();
    assertThat(permutations).containsExactlyInAnyOrder(
      new PrivilegeEscalationVector("foo:*", "bar:foo"),
      new PrivilegeEscalationVector("foo:bar", "bar:*"),
      new PrivilegeEscalationVector("foo:*", "bar:*")
    );
  }

  @Test
  void getWildcardPermutations_with_same_prefix() {
    PrivilegeEscalationVector vector = new PrivilegeEscalationVector("foo:bar", "foo:foo");
    Set<PrivilegeEscalationVector> permutations = vector.getWildcardPermutations();
    assertThat(permutations).containsExactlyInAnyOrder(
      new PrivilegeEscalationVector("foo:*", "foo:foo"),
      new PrivilegeEscalationVector("foo:bar", "foo:*"),
      new PrivilegeEscalationVector("foo:*"));
  }

  @Test
  void appliesToActionPermissions() {
    PrivilegeEscalationVector vector = new PrivilegeEscalationVector("foo:bar", "bar:foo");

    assertThat(vector.appliesToActionPermissions(Set.of("foo:bar", "bar:foo"))).isTrue();
    assertThat(vector.appliesToActionPermissions(Set.of("foo:foo", "foo:bar", "bar:bar", "bar:foo"))).isTrue();
    assertThat(vector.appliesToActionPermissions(Set.of("foo:bar"))).isFalse();
  }

  @Test
  void getEscalationVectorsWithWildcard() {
    Set<PrivilegeEscalationVector> vectors = Set.of(new PrivilegeEscalationVector("foo:bar"), new PrivilegeEscalationVector("bar:foo"));
    assertThat(PrivilegeEscalationVector.getEscalationVectorsWithWildcard(vectors))
      .containsExactlyInAnyOrder(
        new PrivilegeEscalationVector("foo:bar"),
        new PrivilegeEscalationVector("bar:foo"),
        new PrivilegeEscalationVector("foo:*"),
        new PrivilegeEscalationVector("bar:*"));
  }

  @Test
  void test_equals() {
    assertThat(new PrivilegeEscalationVector("foo:bar").equals(new PrivilegeEscalationVector("foo:bar"))).isTrue();

    PrivilegeEscalationVector vector = new PrivilegeEscalationVector("foo:bar");
    assertThat(vector.equals(vector)).isTrue();

    assertThat(vector.equals(vector)).isTrue();
    assertThat(new PrivilegeEscalationVector("foo:bar").equals(new PrivilegeEscalationVector("bar:foo"))).isFalse();
    assertThat(new PrivilegeEscalationVector("foo:bar").equals(null)).isFalse();
    assertThat(new PrivilegeEscalationVector("foo:bar").equals("string")).isFalse();
  }

  @Test
  void test_hashCode() {
    assertThat(new PrivilegeEscalationVector("foo:bar")).hasSameHashCodeAs(new PrivilegeEscalationVector("foo:bar"));
  }
}
