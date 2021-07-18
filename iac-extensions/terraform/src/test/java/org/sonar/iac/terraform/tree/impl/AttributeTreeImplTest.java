/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeTreeImplTest extends TerraformTreeModelTest{

  @Test
  void simple_attribute() {
    AttributeTree tree = parse("a = true", HclLexicalGrammar.ATTRIBUTE);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.ATTRIBUTE);
    assertThat(tree.key().value()).isEqualTo("a");
    assertThat(tree.equalSign().value()).isEqualTo("=");
    assertThat(tree.value()).isInstanceOfSatisfying(LiteralExprTree.class, a -> assertThat(a.value()).isEqualTo("true"));
  }
}
