/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeAccessTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_attribute_access() {
    AttributeAccessTree tree = parse("a.b", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.ATTRIBUTE_ACCESS);
    assertThat(tree.attribute().value()).isEqualTo("b");
    assertThat(tree.accessToken()).isInstanceOfSatisfying(SyntaxToken.class, s -> assertThat(s.value()).isEqualTo("."));
    assertThat(tree.object()).isInstanceOfSatisfying(VariableExprTreeImpl.class, o -> assertThat(o.name()).isEqualTo("a"));
  }

  @Test
  void double_attribute_access() {
    AttributeAccessTreeImpl tree = parse("a.b.c", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.attribute().value()).isEqualTo("c");
    assertThat(tree.object()).isInstanceOfSatisfying(AttributeAccessTreeImpl.class, o -> {
      assertThat(o.attribute().value()).isEqualTo("b");
      assertThat(o.object()).isInstanceOfSatisfying(VariableExprTreeImpl.class, ob -> assertThat(ob.name()).isEqualTo("a"));
    });
  }

  @Test
  void index_access_object() {
    AttributeAccessTree tree = parse("a[1].c", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.attribute().value()).isEqualTo("c");
    assertThat(tree.object()).isInstanceOfSatisfying(IndexAccessExprTreeImpl.class, o -> {
      assertThat(o.subject()).isInstanceOfSatisfying(VariableExprTreeImpl.class, ob -> assertThat(ob.name()).isEqualTo("a"));
      assertThat(o.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, ob -> assertThat(ob.value()).isEqualTo("1"));
    });
  }

  @Test
  void legacy_index_access() {
    AttributeAccessTree tree = parse("a.0", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.attribute().value()).isEqualTo("0");
    assertThat(tree.object()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
  }
}
