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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.KeyValuePairAssert;
import org.sonar.iac.docker.tree.api.LabelInstruction;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.OnBuildInstruction;
import org.sonar.iac.docker.tree.api.StopSignalInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class OnBuildInstructionImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.ONBUILD)
      .matches("ONBUILD STOPSIGNAL SIGKILL")
      .matches("onbuild STOPSIGNAL SIGKILL")
      .matches("ONBUILD ONBUILD STOPSIGNAL SIGKILL")
      .matches("ONBUILD LABEL key1=value1")
      .matches("      ONBUILD LABEL key1=value1")
      .matches("ONBUILD ENTRYPOINT command param1 param2")

      .notMatches("ONBUILD \"LABEL key1=value1\"")
      .notMatches("ONBUILDD LABEL key1=value1")
      .notMatches("ONBUILD")
      .notMatches("ONBUILD ")
      .notMatches("ONBUILD ONBUILD")
      .notMatches("ONBUILD ONBUILD STOPSIGNAL")
      .notMatches("ONBUILD unknown");
  }

  @Test
  void onbuildInstructionSimple() {
    OnBuildInstruction tree = parse("ONBUILD STOPSIGNAL SIGKILL", DockerLexicalGrammar.ONBUILD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ONBUILD);
    assertThat(tree.keyword().value()).isEqualTo("ONBUILD");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 26);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.instruction()).isInstanceOf(StopSignalInstruction.class);

    StopSignalInstruction stopSignal = (StopSignalInstruction) tree.instruction();
    assertThat(stopSignal.getKind()).isEqualTo(DockerTree.Kind.STOPSIGNAL);
    assertThat(stopSignal.keyword().value()).isEqualTo("STOPSIGNAL");

    assertThat(stopSignal.signal().expressions()).satisfies(expressions -> {
      assertThat(expressions).hasSize(1);
      assertThat(expressions.get(0).getKind()).isEqualTo(DockerTree.Kind.STRING_LITERAL);
      assertThat((Literal) expressions.get(0)).extracting(Literal::value).isEqualTo("SIGKILL");
    });

    assertThat(((SyntaxToken) stopSignal.children().get(0)).value()).isEqualTo("STOPSIGNAL");
  }

  @Test
  void onbuildInstructionWithKeyValuePairArgument() {
    OnBuildInstruction tree = parse("ONBUILD LABEL key1=value1 key2=value2", DockerLexicalGrammar.ONBUILD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ONBUILD);
    assertThat(tree.keyword().value()).isEqualTo("ONBUILD");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 37);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.instruction()).isInstanceOf(LabelInstruction.class);

    LabelInstruction label = (LabelInstruction) tree.instruction();
    assertThat(label.getKind()).isEqualTo(DockerTree.Kind.LABEL);
    assertThat(label.keyword().value()).isEqualTo("LABEL");
    assertThat(label.labels()).hasSize(2);
    assertThat(label.textRange()).hasRange(1, 8, 1, 37);
    assertThat(label.children()).hasSize(7);

    List<KeyValuePair> labels = label.labels();
    KeyValuePairAssert.assertThat(labels.get(0)).hasKey("key1").hasValue("value1");
    KeyValuePairAssert.assertThat(labels.get(1)).hasKey("key2").hasValue("value2");
  }

  @Test
  void onbuildRecursive() {
    OnBuildInstruction tree = parse("ONBUILD ONBUILD STOPSIGNAL SIGKILL", DockerLexicalGrammar.ONBUILD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ONBUILD);
    assertThat(tree.keyword().value()).isEqualTo("ONBUILD");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 34);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.instruction()).isInstanceOf(OnBuildInstruction.class);

    OnBuildInstruction onBuild = (OnBuildInstruction) tree.instruction();
    assertThat(onBuild.getKind()).isEqualTo(DockerTree.Kind.ONBUILD);
    assertThat(onBuild.keyword().value()).isEqualTo("ONBUILD");
    assertThat(onBuild.textRange()).hasRange(1, 8, 1, 34);
    assertThat(onBuild.children()).hasSize(2);
    assertThat(onBuild.instruction()).isInstanceOf(StopSignalInstruction.class);

    StopSignalInstruction stopSignal = (StopSignalInstruction) onBuild.instruction();
    assertThat(stopSignal.getKind()).isEqualTo(DockerTree.Kind.STOPSIGNAL);
    assertThat(stopSignal.keyword().value()).isEqualTo("STOPSIGNAL");
    assertThat(stopSignal.signal().expressions()).hasSize(1);
    assertThat(stopSignal.signal().expressions().get(0).getKind()).isEqualTo(DockerTree.Kind.STRING_LITERAL);
    assertThat(((Literal) stopSignal.signal().expressions().get(0)).value()).isEqualTo("SIGKILL");
    assertThat(((SyntaxToken) stopSignal.children().get(0)).value()).isEqualTo("STOPSIGNAL");
    assertThat(((Literal) ((Argument) stopSignal.children().get(1)).expressions().get(0)).value()).isEqualTo("SIGKILL");
  }
}
