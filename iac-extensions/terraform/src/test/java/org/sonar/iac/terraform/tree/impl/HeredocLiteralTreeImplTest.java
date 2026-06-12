/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.HeredocLiteralTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class HeredocLiteralTreeImplTest extends TerraformTreeModelTest {

  @Test
  void shouldExposeRawBodyAsLiteralWhenContentIsNotJson() {
    HeredocLiteralTree tree = parse("<<EOF\nplain text body\nEOF", HclLexicalGrammar.LITERAL_EXPRESSION);

    // The heredoc itself is still a LiteralExprTree with the full original text (markers included).
    assertThat(tree.getKind()).isEqualTo(Kind.HEREDOC_LITERAL);
    assertThat(tree.value()).isEqualTo("<<EOF\nplain text body\nEOF");

    // The nested content is a LiteralExprTree with the body only (no markers).
    assertThat(tree.content()).isInstanceOf(LiteralExprTree.class);
    assertThat(((LiteralExprTree) tree.content()).value()).isEqualTo("plain text body");
  }

  @Test
  void shouldExposeJsonObjectBodyAsObjectTree() {
    HeredocLiteralTree tree = parse("""
      <<POLICY
      {
        "Version": "2012-10-17",
        "Statement": [
          { "Effect": "Deny", "Action": "s3:*" }
        ]
      }
      POLICY""", HclLexicalGrammar.LITERAL_EXPRESSION);

    assertThat(tree.getKind()).isEqualTo(Kind.HEREDOC_LITERAL);
    assertThat(tree.content()).isInstanceOf(ObjectTree.class);

    ObjectTree object = (ObjectTree) tree.content();
    assertThat(PropertyUtils.value(object, "Version").flatMap(TextUtils::getValue)).hasValue("2012-10-17");

    assertThat(PropertyUtils.value(object, "Statement")).hasValueSatisfying(statementValue -> assertThat(statementValue).isInstanceOf(TupleTree.class));
    TupleTree statements = (TupleTree) PropertyUtils.value(object, "Statement").orElseThrow();
    assertThat(statements.elements().trees()).hasSize(1);

    ObjectTree statement = (ObjectTree) statements.elements().trees().get(0);
    assertThat(PropertyUtils.value(statement, "Effect").flatMap(TextUtils::getValue)).hasValue("Deny");
    assertThat(PropertyUtils.value(statement, "Action").flatMap(TextUtils::getValue)).hasValue("s3:*");
  }

  @Test
  void shouldExposeNonObjectJsonBodyAsRawLiteral() {
    // A JSON array root (not an object) is not the {...} shape we expose structurally — fall back to raw.
    HeredocLiteralTree tree = parse("<<EOF\n[1, 2, 3]\nEOF", HclLexicalGrammar.LITERAL_EXPRESSION);

    assertThat(tree.content()).isInstanceOf(LiteralExprTree.class);
    assertThat(((LiteralExprTree) tree.content()).value()).isEqualTo("[1, 2, 3]");
  }

  @Test
  void shouldExposeMalformedJsonBodyAsRawLiteral() {
    HeredocLiteralTree tree = parse("<<EOF\n{ not valid json\nEOF", HclLexicalGrammar.LITERAL_EXPRESSION);

    assertThat(tree.content()).isInstanceOf(LiteralExprTree.class);
    assertThat(((LiteralExprTree) tree.content()).value()).isEqualTo("{ not valid json");
  }
}
