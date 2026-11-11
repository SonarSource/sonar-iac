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
package org.sonar.iac.arm.plugin;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.NoSonarVisitor;
import org.sonar.iac.common.testing.AbstractNoSonarVisitorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class BicepNoSonarVisitorTest extends AbstractNoSonarVisitorTest {
  @Override
  protected TreeParser<Tree> treeParser() {
    return BicepParser.create();
  }

  @Override
  protected NoSonarVisitor noSonarVisitor(NoSonarFilter noSonarFilter) {
    return new NoSonarVisitor(noSonarFilter);
  }

  @Override
  protected String languageKey() {
    return ArmLanguage.KEY;
  }

  @Test
  void shouldRegisterLinesOfNoSonarComments() {
    scan("""
      // NOSONAR
      param location string = resourceGroup().location
      param environmentName string // NOSONAR
      /* multi-line
         NOSONAR */
      param appServiceAppName string = 'app-contoso'
      """);
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(1, 3, 5);
    verify(noSonarFilter).noSonarInFile(inputFile, Set.of(1, 3, 5));
  }
}
