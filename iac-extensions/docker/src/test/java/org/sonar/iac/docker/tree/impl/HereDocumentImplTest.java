/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.File;
import org.sonar.iac.docker.tree.api.HereDocument;
import org.sonar.iac.docker.tree.api.ShellCode;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.TestUtils.assertArgumentsValue;

class HereDocumentImplTest {

  @Test
  void shouldParseHereDocForm() {
    Assertions.assertThat(DockerLexicalGrammar.HEREDOC)
      .matches(" <<KEY\n \nKEY")
      .matches(" <<\"KEY\"\n \nKEY")
      .matches(" <<-KEY\n \nKEY")
      .matches(" <<-KEY\nKEY")
      .matches(" <<KEY\nline 1\nKEY")
      .matches(" <<KEY vals\nline 1\nKEY")
      .matches(" <<KEY1 <<KEY2\nKEY1\nKEY2")
      .matches(" <<KEY1 <<KEY2\nline 1\nKEY1\nline 2\nKEY2")
      .matches(" <<KEY\nKEY")

      .notMatches(" <KEY\nline1\nKEY")
      .notMatches("<<KEY\nline1\nKEY")
      .notMatches(" <<KEY\nline1\nKEY value")
      .notMatches(" <<KEY\nline1\nKEYS")
      .notMatches("");
  }

  @Test
  void shouldCheckHereFormTree() {
    HereDocument hereDoc = DockerTestUtils.parse(" <<KEY some arg\nline 1\nKEY", DockerLexicalGrammar.HEREDOC);

    assertThat(hereDoc.getKind()).isEqualTo(DockerTree.Kind.HEREDOCUMENT);
    assertThat(hereDoc.arguments()).hasSize(6);
    assertThat(hereDoc.arguments().get(0).expressions()).hasSize(1);

    assertArgumentsValue(hereDoc.arguments(), "<<KEY", "some", "arg", "line", "1", "KEY");
  }

  @Test
  void shouldParseHeredocWithProperTextRange() {
    String code = """
      FROM scratch

      ENTRYPOINT <<-INPUT
        apt-get install wget
      INPUT""";
    File file = DockerTestUtils.parse(code, DockerLexicalGrammar.FILE);

    assertThat(file.getKind()).isEqualTo(DockerTree.Kind.FILE);
    assertThat(file.body().dockerImages()).hasSize(1);

    DockerImage dockerImage = file.body().dockerImages().get(0);
    assertThat(dockerImage.instructions()).hasSize(1);

    EntrypointInstruction entrypointInstruction = (EntrypointInstruction) dockerImage.instructions().get(0);
    assertThat(entrypointInstruction.code()).isInstanceOfSatisfying(ShellCode.class,
      shellCode -> assertThat(shellCode.code()).isInstanceOfSatisfying(SyntaxToken.class, syntaxToken -> {
        assertThat(syntaxToken.value()).isEqualTo("""
          <<-INPUT
            apt-get install wget
          INPUT""");
        TextRangeAssert.assertThat(syntaxToken.textRange()).hasRange(3, 11, 5, 5);
      }));
  }

  @Test
  void shouldParseEmptyHeredoc() {
    String code = """
      FROM scratch
      ENTRYPOINT <<EOT
      EOT""";
    File file = DockerTestUtils.parse(code, DockerLexicalGrammar.FILE);

    assertThat(file.getKind()).isEqualTo(DockerTree.Kind.FILE);
    assertThat(file.body().dockerImages()).hasSize(1);

    DockerImage dockerImage = file.body().dockerImages().get(0);
    assertThat(dockerImage.instructions()).hasSize(1);

    EntrypointInstruction entrypointInstruction = (EntrypointInstruction) dockerImage.instructions().get(0);
    assertThat(entrypointInstruction.code()).isInstanceOfSatisfying(ShellCode.class,
      shellCode -> assertThat(shellCode.code()).isInstanceOfSatisfying(SyntaxToken.class, syntaxToken -> {
        assertThat(syntaxToken.value()).isEqualTo("""
          <<EOT
          EOT""");
        TextRangeAssert.assertThat(syntaxToken.textRange()).hasRange(2, 11, 3, 3);
      }));
  }
}
