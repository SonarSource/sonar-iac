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
package org.sonar.iac.jvmframeworkconfig.plugin.visitors;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.NoSonarVisitor;
import org.sonar.iac.common.testing.AbstractNoSonarVisitorTest;
import org.sonar.iac.common.yaml.YamlLanguage;
import org.sonar.iac.jvmframeworkconfig.parser.JvmFrameworkConfigParser;
import org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class JvmFrameworkConfigNoSonarVisitorTest extends AbstractNoSonarVisitorTest {
  @Override
  protected String languageKey() {
    return JvmFrameworkConfigExtension.LANGUAGE_KEY;
  }

  @Override
  protected TreeParser<? extends Tree> treeParser() {
    return new JvmFrameworkConfigParser();
  }

  @Override
  protected NoSonarVisitor noSonarVisitor(NoSonarFilter noSonarFilter) {
    return new JvmFrameworkConfigNoSonarVisitor(noSonarFilter);
  }

  @Test
  void shouldNotRegisterAnythingWithoutNoSonarCommentInProperties() {
    scan(
      // language=properties
      """
        # Comment 1
        sonar.host.url=http://localhost:9000
        #---
        spring.profiles.active=profile2
        """,
      "application.properties");

    assertThat(visitor.noSonarLines()).isEmpty();
  }

  @Test
  void shouldCoverNextPropertyWithNoSonarCommentInProperties() {
    language = JvmFrameworkConfigExtension.LANGUAGE_KEY;

    scan(
      // language=properties
      """
        sonar.host.url=http://localhost:9000
        # NOSONAR

        ! another comment
        spring.datasource.password=password
        spring.datasource.url=jdbc:mysql://127.0.0.1:3306/example-database
        """,
      "application.properties");

    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(2, 5);
    verify(noSonarFilter).noSonarInFile(inputFile, Set.of(2, 5));
  }

  @Test
  void shouldCoverEveryLineOfMultiLinePropertyWithNoSonarCommentInProperties() {
    language = JvmFrameworkConfigExtension.LANGUAGE_KEY;

    scan(
      // language=properties
      """
        ! NOSONAR
        spring.datasource.url=jdbc:mysql://127.0.0.1:3306/\\
          example-database
        spring.datasource.password=password
        """,
      "application.properties");

    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(1, 2, 3);
  }

  @Test
  void shouldOnlyCoverCommentLineWithNoSonarCommentInYaml() {
    language = YamlLanguage.KEY;

    scan(
      // language=yaml
      """
        spring:
          datasource:
            # NOSONAR
            password: password
        """,
      "application.yml");

    assertThat(visitor.noSonarLines()).containsExactly(3);
  }
}
