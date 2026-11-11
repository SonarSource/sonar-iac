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
package org.sonar.iac.common.yaml.visitors;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.iac.common.extension.visitors.NoSonarVisitor;
import org.sonar.iac.common.testing.AbstractNoSonarVisitorTest;
import org.sonar.iac.common.yaml.YamlParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class YamlNoSonarVisitorTest extends AbstractNoSonarVisitorTest {

  @Override
  protected YamlParser treeParser() {
    return new YamlParser();
  }

  @Override
  protected NoSonarVisitor noSonarVisitor(NoSonarFilter noSonarFilter) {
    return new YamlNoSonarVisitor(noSonarFilter);
  }

  @Override
  protected String languageKey() {
    return "yaml";
  }

  @Test
  void shouldRegisterLinesOfNoSonarComments() {
    scan("""
      # NOSONAR
      key: value # NOSONAR
      other: value # comment""");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(1, 2);
    verify(noSonarFilter).noSonarInFile(inputFile, Set.of(1, 2));
  }

  @Test
  void shouldNotRegisterAnythingWithoutNoSonarComment() {
    scan("""
      # comment
      key: value # nosonar""");
    assertThat(visitor.noSonarLines()).isEmpty();
    verify(noSonarFilter, never()).noSonarInFile(any(), any());
  }
}
