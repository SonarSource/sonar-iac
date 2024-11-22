/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.terraform.visitors;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.testing.AbstractMetricsTest;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.iac.terraform.plugin.TerraformLanguage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class TerraformMetricsVisitorTest extends AbstractMetricsTest {

  @Override
  protected HclParser treeParser() {
    return new HclParser();
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new TerraformMetricsVisitor(fileLinesContextFactory, noSonarFilter);
  }

  @Override
  protected String languageKey() {
    return TerraformLanguage.KEY;
  }

  @Test
  void emptySource() {
    scan("");
    assertThat(visitor.linesOfCode()).isEmpty();
    assertThat(visitor.commentLines()).isEmpty();
    verify(noSonarFilter).noSonarInFile(inputFile, new HashSet<>());
  }

  @Test
  void linesOfCode() {
    scan("" +
      "a {\n" +
      "   // comment\n" +
      "   b = {}\n" +
      "}");
    assertThat(visitor.linesOfCode()).containsExactly(1, 3, 4);
  }

  @Test
  void commentLines() {
    scan("" +
      "/* comment */ a {\n" +
      "   // comment\n" +
      "   b = {} // comment\n" +
      "}");
    assertThat(visitor.commentLines()).containsExactly(1, 2, 3);
  }

  @Test
  void multiLineComment() {
    scan("" +
      "/*start\n" +
      " a = {}\n" +
      " end\n" +
      "*/");
    assertThat(visitor.commentLines()).containsExactly(1, 2, 3);
    assertThat(visitor.linesOfCode()).isEmpty();
  }

  @Test
  void noSonarLines() {
    scan("" +
      "a {\n" +
      "// NOSONAR comment\n" +
      "b = {}\n" +
      "}");
    assertThat(visitor.noSonarLines()).containsExactly(2);
    Set<Integer> nosonarLines = new HashSet<>();
    nosonarLines.add(2);
    verify(noSonarFilter).noSonarInFile(inputFile, nosonarLines);
  }
}
