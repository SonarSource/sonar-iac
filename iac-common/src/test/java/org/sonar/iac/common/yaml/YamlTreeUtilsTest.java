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
package org.sonar.iac.common.yaml;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.yaml.tree.FileTreeImpl;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.sonar.iac.common.yaml.YamlTreeTestUtils.scalar;
import static org.sonar.iac.common.yaml.YamlTreeTestUtils.sequence;
import static org.sonar.iac.common.yaml.YamlTreeUtils.getListValueElements;
import static org.sonar.iac.common.yaml.YamlTreeUtils.getRawValue;
import static org.sonarsource.analyzer.commons.collections.ListUtils.getLast;

class YamlTreeUtilsTest {

  @Test
  void getListValueElement() {
    assertThat(getListValueElements(scalar(""))).containsExactly("");
    assertThat(getListValueElements(scalar("false"))).containsExactly("false");
    assertThat(getListValueElements(sequence("false", "true", "test"))).containsExactly("false", "true", "test");
    assertThat(getListValueElements(notTextTree())).isEmpty();
    assertThat(getListValueElements(null)).isEmpty();
  }

  @ParameterizedTest
  @MethodSource
  void shouldExtractRawValueFromScalar(String source, @Nullable String expected) {
    var sourceLines = source.lines().toList();
    var scalar = new ScalarTreeImpl(source, ScalarTree.Style.PLAIN,
      new YamlTreeMetadata(null, TextRanges.range(1, 0, sourceLines.size(), getLast(sourceLines).length()), 0, 0, List.of()));
    assertThat(getRawValue(scalar, source)).isEqualTo(Objects.requireNonNullElse(expected, source));
  }

  public static Stream<Arguments> shouldExtractRawValueFromScalar() {
    return Stream.of(
      of("line", null),
      of("line\n", "line"),
      of("line\r\n", "line"),
      of("line1\nline2", null),
      of("line1\r\nline2", null),
      of("\n  line1", null));
  }

  private FileTreeImpl notTextTree() {
    return new FileTreeImpl(null, null);
  }
}
