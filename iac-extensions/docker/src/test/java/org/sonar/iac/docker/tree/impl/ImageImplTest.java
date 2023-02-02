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
import org.sonar.iac.docker.tree.api.Image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ImageImplTest {

  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.IMAGE)
      .matches(" foo")
      .matches(" FOO")
      .matches(" foo-bar")
      .matches(" foo:bar")
      .matches(" foo@bar")
      .matches(" foo:bar@boo") // tag=bar digest=boo
      .matches(" foo@bar:boo") // tag=null digest=bar:boo

      .notMatches("foo")
      .notMatches(" -foo")
      .notMatches(" foo$bar")
    ;
  }

  @Test
  void test() {
    Image image = parse(" foo:bar@boo", DockerLexicalGrammar.IMAGE);
    assertThat(image.getKind()).isEqualTo(DockerTree.Kind.IMAGE);
    assertThat(image.name().value()).isEqualTo("foo");
    assertThat(image.tag()).isEqualTo("bar");
    assertThat(image.digest()).isEqualTo("boo");

    assertTextRange(image.textRange()).hasRange(1, 1, 1, 12);
  }
}
