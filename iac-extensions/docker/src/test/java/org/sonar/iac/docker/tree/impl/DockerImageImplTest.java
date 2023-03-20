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
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExposeInstruction;
import org.sonar.iac.docker.tree.api.File;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.MaintainerInstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.TestUtils.assertFrom;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class DockerImageImplTest {

  @Test
  void simpleImage() {
    DockerImage dockerImage = parse("FROM foobar", DockerLexicalGrammar.DOCKERIMAGE);
    assertThat(dockerImage.getKind()).isEqualTo(DockerTree.Kind.DOCKERIMAGE);
    assertTextRange(dockerImage.textRange()).hasRange(1, 0, 1, 11);
    assertThat(dockerImage.instructions()).isEmpty();

    FromInstruction from = dockerImage.from();
    assertFrom(from, "foobar", null, null, null);
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, ArgumentImpl.class);
  }

  @Test
  void imageWithInstructions() {
    DockerImage dockerImage = parse(code(
      "FROM foobar",
      "MAINTAINER bob",
      "EXPOSE 80"), DockerLexicalGrammar.DOCKERIMAGE);
    assertThat(dockerImage.getKind()).isEqualTo(DockerTree.Kind.DOCKERIMAGE);
    assertTextRange(dockerImage.textRange()).hasRange(1, 0, 3, 9);

    assertThat(dockerImage.children()).hasExactlyElementsOfTypes(FromInstructionImpl.class, MaintainerInstructionImpl.class, ExposeInstructionImpl.class);
    assertThat(dockerImage.instructions()).hasSize(2);
    MaintainerInstruction maintainer = (MaintainerInstruction) dockerImage.instructions().get(0);
    assertThat(maintainer.authors()).extracting(TextTree::value).containsExactly("bob");
    ExposeInstruction expose = (ExposeInstruction) dockerImage.instructions().get(1);
    assertThat(expose.arguments().stream().map(arg -> ArgumentResolution.of(arg).value())).containsExactly("80");
  }

  @Test
  void multipleImagesWithInstructions() {
    File file = parse(code(
      "FROM foo",
      "MAINTAINER bob",
      "EXPOSE 80",
      "FROM bar",
      "USER bob",
      "LABEL key1=value1"), DockerLexicalGrammar.FILE);
    assertThat(file.body().dockerImages()).hasSize(2);

    DockerImage dockerImage1 = file.body().dockerImages().get(0);
    assertTextRange(dockerImage1.textRange()).hasRange(1, 0, 3, 9);
    assertThat(dockerImage1.children()).hasExactlyElementsOfTypes(FromInstructionImpl.class, MaintainerInstructionImpl.class, ExposeInstructionImpl.class);

    DockerImage dockerImage2 = file.body().dockerImages().get(1);
    assertTextRange(dockerImage2.textRange()).hasRange(4, 0, 6, 17);
    assertThat(dockerImage2.children()).hasExactlyElementsOfTypes(FromInstructionImpl.class, UserInstructionImpl.class, LabelInstructionImpl.class);
  }
}
