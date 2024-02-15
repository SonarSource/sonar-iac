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
package org.sonar.iac.common.api.checks;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.checks.CommonTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

class SecondaryLocationTest {

  @Test
  void shouldCreateSecondaryInstance() {
    SecondaryLocation location = new SecondaryLocation(range(2, 5, 8, 12), "message");

    assertThat(location.message).isEqualTo("message");
    assertThat(location.textRange).hasRange(2, 5, 8, 12);
    assertThat(location.filePath).isNull();
  }

  @Test
  void shouldCreateOfInstance() {
    HasTextRange tree = new CommonTestUtils.TestTextTree("value", List.of());
    SecondaryLocation location = new SecondaryLocation(tree, "message", "path/to/file");

    assertThat(location.message).isEqualTo("message");
    assertThat(location.textRange).hasRange(1, 0, 1, 5);
    assertThat(location.filePath).isEqualTo("path/to/file");
  }

  @Test
  void shouldTestEquals() {
    TextRange range = range(2, 5, 8, 12);
    String message = "message";
    String path = "path/to/file";

    SecondaryLocation location1 = new SecondaryLocation(range, message, path);
    SecondaryLocation location2 = new SecondaryLocation(range, message, path);
    SecondaryLocation location3 = new SecondaryLocation(range, message, "otherPath");
    SecondaryLocation location4 = new SecondaryLocation(range, "otherMessage", path);
    SecondaryLocation location5 = new SecondaryLocation(range(0, 1, 2, 3), message, path);
    SecondaryLocation location6 = new SecondaryLocation(range, message);
    SecondaryLocation location7 = new SecondaryLocation(new CommonTestUtils.TestTextTree("value", List.of()), message, path);
    SecondaryLocation location8 = new SecondaryLocation(new CommonTestUtils.TestTextTree("value", List.of()), message);

    assertThat(location1.equals(location1)).isTrue();
    assertThat(location1.equals(location2)).isTrue();
    assertThat(location1.equals(new Object())).isFalse();
    assertThat(location1.equals(null)).isFalse();
    assertThat(location1.equals(location3)).isFalse();
    assertThat(location1.equals(location4)).isFalse();
    assertThat(location1.equals(location5)).isFalse();
    assertThat(location1.equals(location6)).isFalse();
    assertThat(location1.equals(location7)).isFalse();
    assertThat(location8.equals(location7)).isFalse();
  }

  @Test
  void shouldTestHashCode() {
    SecondaryLocation location1 = new SecondaryLocation(range(2, 5, 8, 12), "message", "path/to/file");
    SecondaryLocation location2 = new SecondaryLocation(range(2, 5, 8, 12), "message", "path/to/file");

    assertThat(location1).hasSameHashCodeAs(location2);
  }
}
