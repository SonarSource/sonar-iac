/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.IndexAccessExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class IndexAccessExprTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_index_access() {
    IndexAccessExprTree tree = parse("a[1]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.INDEX_ACCESS_EXPR);
    assertThat(tree.subject()).isInstanceOfSatisfying(VariableExprTreeImpl.class, o -> assertThat(o.name()).isEqualTo("a"));
    assertThat(tree.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> assertThat(o.value()).isEqualTo("1"));
    assertThat(tree.children()).hasSize(4);
  }

  @Test
  void double_index_access() {
    IndexAccessExprTree tree = parse("a[1][2]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.subject()).isInstanceOfSatisfying(IndexAccessExprTreeImpl.class, o -> {
      assertThat(o.subject()).isInstanceOfSatisfying(VariableExprTreeImpl.class, ob -> assertThat(ob.name()).isEqualTo("a"));
      assertThat(o.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, ob -> assertThat(ob.value()).isEqualTo("1"));
      assertThat(o.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, ob -> assertThat(ob.value()).isEqualTo("1"));
    });
    assertThat(tree.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> assertThat(o.value()).isEqualTo("2"));
  }

  @Test
  void attribute_access_subject() {
    IndexAccessExprTree tree = parse("a.b[1]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.subject()).isInstanceOfSatisfying(AttributeAccessTreeImpl.class, o -> {
      assertThat(o.object()).isInstanceOfSatisfying(VariableExprTreeImpl.class, ob -> assertThat(ob.name()).isEqualTo("a"));
      assertThat(o.attribute().value()).isEqualTo("b");
    });
    assertThat(tree.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> assertThat(o.value()).isEqualTo("1"));
  }
}
