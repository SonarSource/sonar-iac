/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.extension.visitors;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThat;

class NoSonarNextLineResolverTest {

  private NoSonarNextLineResolver noSonarNextLineResolver;

  @BeforeEach
  void setUp() {
    noSonarNextLineResolver = new NoSonarNextLineResolver();
  }

  @Test
  void shouldCoverNextCodeLineSkippingBlankAndCommentLines() {
    noSonarNextLineResolver.addCodeLines(List.of(1, 5, 6));

    assertThat(noSonarNextLineResolver.coveredLines(List.of(2))).containsExactly(5);
  }

  @Test
  void shouldCoverEveryLineOfElementStartingOnNextCodeLine() {
    noSonarNextLineResolver.addCodeLines(TextRanges.range(2, 0, 4, 3));
    noSonarNextLineResolver.addElement(TextRanges.range(2, 0, 4, 3));

    assertThat(noSonarNextLineResolver.coveredLines(List.of(1))).containsExactlyInAnyOrder(2, 3, 4);
  }

  @Test
  void shouldKeepLongestElementWhenSeveralStartOnSameLine() {
    noSonarNextLineResolver.addCodeLines(TextRanges.range(2, 0, 5, 3));
    noSonarNextLineResolver.addElement(TextRanges.range(2, 0, 5, 3));
    noSonarNextLineResolver.addElement(TextRanges.range(2, 4, 3, 1));

    assertThat(noSonarNextLineResolver.coveredLines(List.of(1))).containsExactlyInAnyOrder(2, 3, 4, 5);
  }

  @Test
  void shouldCoverOnlyNextCodeLineWhenItIsInsideElement() {
    noSonarNextLineResolver.addCodeLines(List.of(1, 3));
    noSonarNextLineResolver.addElement(TextRanges.range(1, 0, 3, 3));

    assertThat(noSonarNextLineResolver.coveredLines(List.of(2))).containsExactly(3);
  }

  @Test
  void shouldNotCoverNextLineWhenCommentFollowsCode() {
    noSonarNextLineResolver.addCodeLines(List.of(1, 2));

    assertThat(noSonarNextLineResolver.coveredLines(List.of(1))).isEmpty();
  }

  @Test
  void shouldNotCoverAnythingWhenNoCodeFollows() {
    noSonarNextLineResolver.addCodeLine(1);

    assertThat(noSonarNextLineResolver.coveredLines(List.of(2))).isEmpty();
  }

  @Test
  void shouldCoverElementStartingOnCommentLine() {
    noSonarNextLineResolver.addCodeLines(TextRanges.range(2, 0, 4, 3));
    noSonarNextLineResolver.addElement(TextRanges.range(2, 10, 4, 3));

    assertThat(noSonarNextLineResolver.coveredElementLines(List.of(2))).containsExactlyInAnyOrder(2, 3, 4);
  }

  @Test
  void shouldCoverElementStartingOnNextCodeLine() {
    noSonarNextLineResolver.addCodeLines(TextRanges.range(3, 0, 5, 3));
    noSonarNextLineResolver.addElement(TextRanges.range(3, 10, 5, 3));

    assertThat(noSonarNextLineResolver.coveredElementLines(List.of(1))).containsExactlyInAnyOrder(3, 4, 5);
  }

  @Test
  void shouldNotCoverNextCodeLineOutsideElement() {
    noSonarNextLineResolver.addCodeLines(List.of(2, 4, 5));
    noSonarNextLineResolver.addElement(TextRanges.range(4, 0, 5, 3));

    assertThat(noSonarNextLineResolver.coveredElementLines(List.of(1, 2, 3))).containsExactlyInAnyOrder(4, 5);
  }
}
