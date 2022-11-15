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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerKeyword;
import org.sonar.iac.docker.parser.utils.DockerAssertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.StopSignalTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

class StopSignalTreeImplTest {

  @Test
  void test() {
    DockerAssertions.assertThat(DockerKeyword.STOPSIGNAL)
      .matches("STOPSIGNAL SIGKILL")
      .matches("STOPSIGNAL SIGTERM")
      .matches("STOPSIGNAL foo")
      .matches("STOPSIGNAL 9")
      .matches("STOPSIGNAL 1")
      .matches("   STOPSIGNAL 1")
      .notMatches("STOPSIGNALfooo")
      .notMatches("stopsignal")
      .notMatches("stopsignal 9")
      .notMatches("STOPSIGNALL");
  }

  @Test
  void test2() {
    StopSignalTree tree = DockerTestUtils.parse("STOPSIGNAL SIGKILL", DockerKeyword.STOPSIGNAL);
    Assertions.assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.STOPSIGNAL);
    Assertions.assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("STOPSIGNAL");
    Assertions.assertThat(((SyntaxToken)tree.children().get(1)).value()).isEqualTo("SIGKILL");
  }
}
