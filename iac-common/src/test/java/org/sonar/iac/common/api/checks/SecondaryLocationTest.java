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
package org.sonar.iac.common.api.checks;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.checks.CommonTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

class SecondaryLocationTest {

  @Test
  void shouldCreateSecondaryInstance() {
    SecondaryLocation location = SecondaryLocation.secondary(2, 5, 8, 12, "message");

    assertThat(location.message).isEqualTo("message");
    assertThat(location.textRange).hasRange(2, 5, 8, 12);
  }

  @Test
  void shouldCreateOfInstance() {
    HasTextRange tree = new CommonTestUtils.TestTextTree("value", List.of());
    SecondaryLocation location = SecondaryLocation.of(tree, "message");

    assertThat(location.message).isEqualTo("message");
    assertThat(location.textRange).hasRange(1, 0, 1, 5);
  }

  @Test
  void shouldTestEquals() {
    SecondaryLocation location1 = SecondaryLocation.secondary(2, 5, 8, 12, "message");
    SecondaryLocation location2 = SecondaryLocation.secondary(2, 5, 8, 12, "message");
    SecondaryLocation location3 = SecondaryLocation.secondary(2, 5, 8, 12, "abc");
    SecondaryLocation location4 = SecondaryLocation.secondary(0, 1, 2, 3, "message");

    assertThat(location1.equals(location1)).isTrue();
    assertThat(location1.equals(location2)).isTrue();
    assertThat(location1.equals(new Object())).isFalse();
    assertThat(location1.equals(null)).isFalse();
    assertThat(location1.equals(location3)).isFalse();
    assertThat(location1.equals(location4)).isFalse();
  }

  @Test
  void shouldTestHashCode() {
    SecondaryLocation location1 = SecondaryLocation.secondary(2, 5, 8, 12, "message");
    SecondaryLocation location2 = SecondaryLocation.secondary(2, 5, 8, 12, "message");

    assertThat(location1).hasSameHashCodeAs(location2.hashCode());
  }
}
