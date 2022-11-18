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
import org.sonar.iac.docker.tree.api.KeyValuePairTree;
import org.sonar.iac.docker.tree.api.LabelTree;
import org.sonar.iac.docker.tree.api.OnBuildTree;
import org.sonar.iac.docker.tree.api.StopSignalTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class OnBuildTreeImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.ONBUILD)
      .matches("ONBUILD STOPSIGNAL SIGKILL")
      .matches("onbuild STOPSIGNAL SIGKILL")
      .matches("ONBUILD ONBUILD STOPSIGNAL SIGKILL")
      .matches("ONBUILD LABEL key1=value1")
      .matches("      ONBUILD LABEL key1=value1")
      .notMatches("ONBUILD \"LABEL key1=value1\"")
      .notMatches("ONBUILDD LABEL key1=value1")
      .notMatches("ONBUILD")
      .notMatches("ONBUILD ")
      .notMatches("ONBUILD ONBUILD")
      .notMatches("ONBUILD ONBUILD STOPSIGNAL")
      .notMatches("ONBUILD unknown")
    ;
  }

  @Test
  void onbuildInstructionSimple() {
    OnBuildTree tree = parse("ONBUILD STOPSIGNAL SIGKILL", DockerLexicalGrammar.ONBUILD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ONBUILD);
    assertThat(tree.keyword().value()).isEqualTo("ONBUILD");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 26);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.instruction()).isInstanceOf(StopSignalTree.class);

    StopSignalTree stopSignal = (StopSignalTree) tree.instruction();
    assertThat(stopSignal.getKind()).isEqualTo(DockerTree.Kind.STOPSIGNAL);
    assertThat(stopSignal.keyword().value()).isEqualTo("STOPSIGNAL");
    assertThat(stopSignal.signal().value()).isEqualTo("SIGKILL");
    assertThat(((SyntaxToken)stopSignal.children().get(0)).value()).isEqualTo("STOPSIGNAL");
    assertThat(((SyntaxToken)stopSignal.children().get(1)).value()).isEqualTo("SIGKILL");
  }

  @Test
  void onbuildInstructionWithKeyValuePairArgument() {
    OnBuildTree tree = parse("ONBUILD LABEL key1=value1 key2=value2", DockerLexicalGrammar.ONBUILD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ONBUILD);
    assertThat(tree.keyword().value()).isEqualTo("ONBUILD");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 37);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.instruction()).isInstanceOf(LabelTree.class);

    LabelTree label = (LabelTree) tree.instruction();
    assertThat(label.getKind()).isEqualTo(DockerTree.Kind.LABEL);
    assertThat(label.keyword().value()).isEqualTo("LABEL");
    assertThat(label.keyValuePairs()).hasSize(2);
    assertTextRange(label.textRange()).hasRange(1, 8, 1, 37);
    assertThat(label.children()).hasSize(7);

    KeyValuePairTree keyValuePair1 = label.keyValuePairs().get(0);
    assertThat(keyValuePair1.key().value()).isEqualTo("key1");
    assertThat(keyValuePair1.equals().value()).isEqualTo("=");
    assertThat(keyValuePair1.value().value()).isEqualTo("value1");
    KeyValuePairTree keyValuePair2 = label.keyValuePairs().get(1);
    assertThat(keyValuePair2.key().value()).isEqualTo("key2");
    assertThat(keyValuePair2.equals().value()).isEqualTo("=");
    assertThat(keyValuePair2.value().value()).isEqualTo("value2");
  }

  @Test
  void onbuildRecursive() {
    OnBuildTree tree = parse("ONBUILD ONBUILD STOPSIGNAL SIGKILL", DockerLexicalGrammar.ONBUILD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ONBUILD);
    assertThat(tree.keyword().value()).isEqualTo("ONBUILD");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 34);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.instruction()).isInstanceOf(OnBuildTree.class);

    OnBuildTree onBuild = (OnBuildTree) tree.instruction();
    assertThat(onBuild.getKind()).isEqualTo(DockerTree.Kind.ONBUILD);
    assertThat(onBuild.keyword().value()).isEqualTo("ONBUILD");
    assertTextRange(onBuild.textRange()).hasRange(1, 8, 1, 34);
    assertThat(onBuild.children()).hasSize(2);
    assertThat(onBuild.instruction()).isInstanceOf(StopSignalTree.class);

    StopSignalTree stopSignal = (StopSignalTree) onBuild.instruction();
    assertThat(stopSignal.getKind()).isEqualTo(DockerTree.Kind.STOPSIGNAL);
    assertThat(stopSignal.keyword().value()).isEqualTo("STOPSIGNAL");
    assertThat(stopSignal.signal().value()).isEqualTo("SIGKILL");
    assertThat(((SyntaxToken)stopSignal.children().get(0)).value()).isEqualTo("STOPSIGNAL");
    assertThat(((SyntaxToken)stopSignal.children().get(1)).value()).isEqualTo("SIGKILL");
  }
}
