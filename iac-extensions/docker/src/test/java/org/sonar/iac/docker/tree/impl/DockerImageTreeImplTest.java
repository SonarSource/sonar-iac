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
import org.sonar.iac.docker.tree.api.DockerImageTree;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExposeTree;
import org.sonar.iac.docker.tree.api.FileTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.ImageTree;
import org.sonar.iac.docker.tree.api.MaintainerTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class DockerImageTreeImplTest {

  @Test
  void simpleImage() {
    DockerImageTree dockerImage = parse("FROM foobar", DockerLexicalGrammar.DOCKERIMAGE);
    assertThat(dockerImage.getKind()).isEqualTo(DockerTree.Kind.DOCKERIMAGE);
    assertTextRange(dockerImage.textRange()).hasRange(1, 0, 1, 11);
    assertThat(dockerImage.instructions()).isEmpty();

    FromTree from = dockerImage.from();
    assertThat(from.keyword().value()).isEqualTo("FROM");
    assertThat(from.alias()).isNull();
    assertThat(from.platform()).isNull();
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, ImageTreeImpl.class);
    ImageTree image = from.image();
    assertThat(image.name().value()).isEqualTo("foobar");
    assertThat(image.tag()).isNull();
    assertThat(image.digest()).isNull();
  }

  @Test
  void imageWithInstructions() {
    DockerImageTree dockerImage = parse("FROM foobar\nMAINTAINER bob\nEXPOSE 80", DockerLexicalGrammar.DOCKERIMAGE);
    assertThat(dockerImage.getKind()).isEqualTo(DockerTree.Kind.DOCKERIMAGE);
    assertTextRange(dockerImage.textRange()).hasRange(1, 0, 3, 9);

    assertThat(dockerImage.children()).hasExactlyElementsOfTypes(FromTreeImpl.class, MaintainerTreeImpl.class, ExposeTreeImpl.class);
    assertThat(dockerImage.instructions()).hasSize(2);
    MaintainerTree maintainer = (MaintainerTree) dockerImage.instructions().get(0);
    assertThat(maintainer.authors()).hasSize(1);
    assertThat(maintainer.authors().get(0).value()).isEqualTo("bob");
    ExposeTree expose = (ExposeTree) dockerImage.instructions().get(1);
    assertThat(expose.ports()).hasSize(1);
    assertThat(expose.ports().get(0).portMin().value()).isEqualTo("80");
  }

  @Test
  void multipleImagesWithInstructions() {
    FileTree fileTree = parse("FROM foo\nMAINTAINER bob\nEXPOSE 80\nFROM bar\nUSER bob\nLABEL key1=value1", DockerLexicalGrammar.FILE);
    assertThat(fileTree.dockerImages()).hasSize(2);

    DockerImageTree dockerImage1 = fileTree.dockerImages().get(0);
    assertTextRange(dockerImage1.textRange()).hasRange(1, 0, 3, 9);
    assertThat(dockerImage1.children()).hasExactlyElementsOfTypes(FromTreeImpl.class, MaintainerTreeImpl.class, ExposeTreeImpl.class);

    DockerImageTree dockerImage2 = fileTree.dockerImages().get(1);
    assertTextRange(dockerImage2.textRange()).hasRange(4, 0, 6, 17);
    assertThat(dockerImage2.children()).hasExactlyElementsOfTypes(FromTreeImpl.class, UserTreeImpl.class, LabelTreeImpl.class);
  }
}
