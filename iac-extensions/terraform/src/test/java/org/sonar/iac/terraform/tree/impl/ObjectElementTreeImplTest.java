/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectElementTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_element() {
    ObjectElementTree tree = parse("a : 1", HclLexicalGrammar.OBJECT_ELEMENT);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.OBJECT_ELEMENT);
    assertThat(tree.key()).isInstanceOfSatisfying(VariableExprTreeImpl.class, n -> assertThat(n.name()).isEqualTo("a"));
    assertThat(tree.equalOrColonSign()).isInstanceOfSatisfying(SyntaxTokenImpl.class, n -> assertThat(n.value()).isEqualTo(":"));
    assertThat(tree.value()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, n -> assertThat(n.value()).isEqualTo("1"));
  }
}
