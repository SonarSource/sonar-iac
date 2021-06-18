/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.AttributeSplatAccessTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeSplatAccessTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_attribute_splat_access() {
    AttributeSplatAccessTree tree = parse("a.*", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.ATTRIBUTE_SPLAT_ACCESS);
    assertThat(tree.children()).hasSize(3);
    assertThat(tree.object()).isInstanceOfSatisfying(VariableExprTreeImpl.class, o -> assertThat(o.name()).isEqualTo("a"));
  }
}
