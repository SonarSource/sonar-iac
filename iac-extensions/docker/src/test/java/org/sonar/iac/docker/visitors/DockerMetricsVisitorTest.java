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
package org.sonar.iac.docker.visitors;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.testing.AbstractMetricsTest;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.plugin.DockerLanguage;
import org.sonar.scanner.plugin.api.impl.config.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class DockerMetricsVisitorTest extends AbstractMetricsTest {

  @Override
  protected TreeParser<Tree> treeParser() {
    return DockerParser.create();
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new DockerMetricsVisitor(fileLinesContextFactory, noSonarFilter, sensorTelemetry);
  }

  @Override
  protected String languageKey() {
    return new DockerLanguage(new MapSettings().asConfig()).getKey();
  }

  @Test
  void linesOfCode() {
    scan("""
      FROM foo

      MAINTAINER foo<bar>

      RUN \\
        command1 \\
        command2""");
    assertThat(visitor.linesOfCode()).containsExactly(1, 3, 5, 6, 7);
    verifyLinesOfCodeMetricsAndTelemetry(1, 3, 5, 6, 7);
  }

  @Test
  void shouldCoverWholeNextInstructionWithFullLineNoSonarComment() {
    scan("""
      FROM foo
      # NOSONAR

      COPY --chmod=777 \\
           src/ \\
           /app/
      USER root""");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(2, 4, 5, 6);
    verify(noSonarFilter).noSonarInFile(inputFile, Set.of(2, 4, 5, 6));
  }

  @Test
  void shouldCoverSingleLineInstructionWithFullLineNoSonarComment() {
    scan("""
      FROM foo
      # NOSONAR
      USER root
      RUN chmod 777 /app""");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(2, 3);
  }

  @Test
  void shouldCoverNextLineOnlyWithNoSonarCommentInsideRunContinuation() {
    scan("""
      FROM foo
      RUN apt-get update && \\
          # NOSONAR
          chmod 777 /app/data && \\
          rm -rf /tmp/cache
      USER root""");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(3, 4);
  }

  @Test
  void shouldCoverNextLineWithNoSonarCommentInsideCopyContinuation() {
    scan("""
      FROM foo
      COPY --chmod=777 \\
           # NOSONAR
           src/ \\
           /app/""");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(3, 4);
  }

  @Test
  void shouldCoverWholeHeredocInstructionWithFullLineNoSonarComment() {
    scan("""
      FROM foo
      # NOSONAR
      RUN <<EOF
      chmod 777 /app/data
      EOF
      USER root""");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(2, 3, 4, 5);
  }

  @Test
  void shouldCoverNextLineWithNoSonarCommentInsideHeredoc() {
    scan("""
      FROM foo
      RUN <<EOF
      apt-get update
      # NOSONAR
      chmod 777 /app/data
      EOF""");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(4, 5);
  }

  @Test
  void shouldCoverNextInstructionWithWindowsLineEndings() {
    scan("FROM foo\r\n# NOSONAR\r\nUSER root\r\n");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(2, 3);
  }

  @Test
  void shouldOnlyRegisterOwnLineWhenNoCodeFollowsNoSonarComment() {
    scan("""
      FROM foo
      # NOSONAR
      """);
    assertThat(visitor.noSonarLines()).containsExactly(2);
  }

  @Test
  void shouldNotRegisterLinesWithoutNoSonarComment() {
    scan("""
      # comment
      FROM foo
      # nosonar
      USER root""");
    assertThat(visitor.noSonarLines()).isEmpty();
  }
}
