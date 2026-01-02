/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Flag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class FlagImplTest {

  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.FLAG)
      .matches(" --platform=foo")
      .matches(" --platform=FOO")
      .matches(" --chown=55:mygroup")
      .matches(" --hello-world=foo")
      .matches(" --chown=55:mygroup")
      .matches(" --mount=type=secret,id=build_secret,mode=0666")
      .matches(" --platform=")
      .matches(" --platform=\"foo\"")
      .matches(" --platform=\"foo\"${val}other")

      .notMatches("--platform=foo")
      .notMatches(" -platform=foo")
      .notMatches(" platform=foo")
      .notMatches(" --platform= foo")
      .notMatches(" --platform foo")
      .notMatches(" --PLATFORM=foo")
      .notMatches(" --platform=foo\n");
  }

  @Test
  void test() {
    Flag param = parse(" --platform=foo", DockerLexicalGrammar.FLAG);
    assertThat(param.getKind()).isEqualTo(DockerTree.Kind.PARAM);
    assertThat(param.name()).isEqualTo("platform");
    assertThat(ArgumentResolution.of(param.value()).value()).isEqualTo("foo");
    assertThat(param.textRange()).hasRange(1, 1, 1, 15);
  }

  @Test
  void shouldConvertToString() {
    Flag param = parse(" --platform=foo", DockerLexicalGrammar.FLAG);
    assertThat(param).hasToString("FlagImpl{prefix=--, name=platform, equals==, value=foo}");
  }

  @Test
  void shouldCheckEquality() {
    Flag param1 = parse(" --platform=foo", DockerLexicalGrammar.FLAG);
    Flag param2 = parse(" --platform=foo", DockerLexicalGrammar.FLAG);
    Flag param3 = parse(" --platform=bar", DockerLexicalGrammar.FLAG);
    Flag param4 = parse(" --option=baz", DockerLexicalGrammar.FLAG);

    assertThat(param1)
      .isEqualTo(param1)
      .isEqualTo(param2)
      .hasSameHashCodeAs(param2)
      .isNotEqualTo(param3)
      .isNotEqualTo(param4)
      .doesNotHaveSameHashCodeAs(param3)
      .isNotEqualTo(null)
      .isNotEqualTo(new Object());
  }
}
