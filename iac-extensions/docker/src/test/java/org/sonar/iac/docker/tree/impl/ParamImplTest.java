/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Param;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ParamImplTest {

  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.PARAM)
      .matches(" --platform=foo")
      .matches(" --platform=FOO")
      .matches(" --chown=55:mygroup")
      .matches(" --hello-world=foo")
      .matches(" --chown=55:mygroup")
      .matches(" --mount=type=secret,id=build_secret,mode=0666")
      .matches(" --platform=")

      .notMatches("--platform=foo")
      .notMatches(" -platform=foo")
      .notMatches(" platform=foo")
      .notMatches(" --platform= foo")
      .notMatches(" --platform foo")
      .notMatches(" --PLATFORM=foo")
      .notMatches(" --platform=foo\n")
    ;
  }

  @Test
  void test() {
    Param param = parse(" --platform=foo", DockerLexicalGrammar.PARAM);
    assertThat(param.getKind()).isEqualTo(DockerTree.Kind.PARAM);
    assertThat(param.name()).isEqualTo("platform");
    assertThat(param.value().value()).isEqualTo("foo");
    assertTextRange(param.textRange()).hasRange(1, 1, 1, 15);
  }

}
