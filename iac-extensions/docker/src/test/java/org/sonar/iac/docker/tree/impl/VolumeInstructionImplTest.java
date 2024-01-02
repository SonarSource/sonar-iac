/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.VolumeInstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.TestUtils.assertArgumentsValue;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class VolumeInstructionImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.VOLUME)
      .matches("VOLUME /var/log")
      .matches("VOLUME /var/log /var/db")
      .matches("VOLUME 80")
      .matches("    VOLUME /var/log")
      .matches("volume \"/var/log\"")
      .matches("VOLUME [\"/var/log\"]")
      .matches("VOLUME [\"/var/log\", \"/var/db\"]")
      .matches("VOLUME $myvolume")
      .matches("VOLUME ${myvolume}")
      .matches("VOLUME ${myvolume:-test}")
      .notMatches("VOLUME ${myvolume%%[a-z]+}")
      .notMatches("VOLUME")
      .notMatches("VOLUME ")
      .notMatches("VOLUMEE 80");
  }

  @Test
  void volumeInstructionShell() {
    VolumeInstruction tree = parse("VOLUME /var/log /var/db", DockerLexicalGrammar.VOLUME);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.VOLUME);
    assertThat(tree.keyword().value()).isEqualTo("VOLUME");
    assertArgumentsValue(tree.arguments(), "/var/log", "/var/db");
  }

  @Test
  void volumeInstructionExec() {
    VolumeInstruction tree = parse("VOLUME [\"/var/log\", \"/var/db\"]", DockerLexicalGrammar.VOLUME);
    assertThat(tree.arguments().stream().map(arg -> ArgumentResolution.of(arg).value())).containsExactly("/var/log", "/var/db");
  }

  @Test
  void volumeInstructionEmptyExec() {
    VolumeInstruction tree = parse("VOLUME []", DockerLexicalGrammar.VOLUME);
    assertThat(tree.arguments()).isEmpty();
  }
}
