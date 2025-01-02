/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.IacToken;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.testing.AbstractMetricsTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class MetricsVisitorTest extends AbstractMetricsTest {

  @Override
  protected TreeParser<Tree> treeParser() {
    return (source, inputFileContext) -> new TestToken("dummy value");
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new MetricsVisitor(fileLinesContextFactory, noSonarFilter, sensorTelemetryMetrics) {
    };
  }

  @Override
  protected String languageKey() {
    return "txt";
  }

  @Test
  void shouldTestMetrics() {
    MetricsVisitor visitor = scan("#comment\nfoo\n \n#NOSONAR\n#");
    assertThat(visitor.commentLines()).containsExactly(1);
    assertThat(visitor.linesOfCode()).containsExactly(2);
    assertThat(visitor.noSonarLines()).containsExactly(3);
    Set<Integer> nosonarLines = new HashSet<>();
    nosonarLines.add(3);
    verify(noSonarFilter).noSonarInFile(inputFile, nosonarLines);
    verifyLinesOfCodeMetricsAndTelemetry(2);
  }

  @Test
  void shouldTestEmptyToken() {
    parser = (source, inputFileContext) -> new TestToken("");
    MetricsVisitor visitor = scan("");
    assertThat(visitor.linesOfCode()).isEmpty();
    verifyLinesOfCodeMetricsAndTelemetry();
  }

  @Test
  void shouldTestWhitespaceToken() {
    parser = (source, inputFileContext) -> new TestToken(" ");
    MetricsVisitor visitor = scan(" ");
    assertThat(visitor.linesOfCode()).isEmpty();
    verifyLinesOfCodeMetricsAndTelemetry();
  }

  static class TestToken implements IacToken {

    private final String value;

    public TestToken(String value) {
      this.value = value;
    }

    @Override
    public List<Comment> comments() {
      return Arrays.asList(
        new CommentImpl("#comment", "comment", TextRanges.range(1, 0, "#comment")),
        new CommentImpl("#NOSONAR", "NOSONAR", TextRanges.range(3, 0, "#NOSONAR")),
        new CommentImpl("#", "", TextRanges.range(4, 0, "#")));
    }

    @Override
    public TextRange textRange() {
      return TextRanges.range(2, 0, "foo");
    }

    @Override
    public List<Tree> children() {
      return Collections.emptyList();
    }

    @Override
    public String value() {
      return value;
    }
  }
}
