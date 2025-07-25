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
package org.sonar.iac.docker.visitors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.testing.AbstractMetricsTest;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.plugin.DockerLanguage;

import static org.assertj.core.api.Assertions.assertThat;

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
  @Disabled("Will be fixed with SONARIAC-606")
  // TODO SONARIAC-606
  void commentLines() {
    scan("""
      # comment 1
      # comment 2
      FROM foo
      RUN \\
        command1 \\
        # comment 3
        command2""");
    assertThat(visitor.commentLines()).containsExactly(1, 2, 6);
    verifyLinesOfCodeMetricsAndTelemetry(1, 2, 6);
  }
}
