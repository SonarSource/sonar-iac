/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.common.api.tree.Comment;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class CommentTest extends BicepTreeModelTest {

  @Test
  void shouldParseSimpleVariableDeclarationWithSingleLineComment() {
    VariableDeclaration tree = parse("// Comment content\nvar foo = 42", BicepLexicalGrammar.VARIABLE_DECLARATION);

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("var", "foo", "=", "42");
    List<Comment> comments = ((SyntaxTokenImpl) tree.children().get(0)).comments();
    assertThat(comments).hasSize(1);
    assertThat(comments.get(0).value()).isEqualTo("// Comment content");
    assertThat(comments.get(0).contentText()).isEqualTo(" Comment content");
  }

  @Test
  void shouldParseSimpleVariableDeclarationWithMultilineComment() {
    VariableDeclaration tree = parse("/* Comment content line\n  content line 2 */\nvar foo = 42", BicepLexicalGrammar.VARIABLE_DECLARATION);

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("var", "foo", "=", "42");
    List<Comment> comments = ((SyntaxTokenImpl) tree.children().get(0)).comments();
    assertThat(comments).hasSize(1);
    assertThat(comments.get(0).value()).isEqualTo("/* Comment content line\n  content line 2 */");
    assertThat(comments.get(0).contentText()).isEqualTo(" Comment content line\n  content line 2 ");
  }
}
