package org.sonar.plugins.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.plugins.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class LiteralExprTreeImplTest extends TerraformTreeModelTest {
  @Test
  void boolean_literal() {
    LiteralExprTree tree = parse("true", HclLexicalGrammar.LITERAL_EXPRESSION);
    assertThat(tree).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> {
      assertThat(o.value()).isEqualTo("true");
      assertThat(o.value()).isEqualTo(o.token().text());
    });
  }
}
