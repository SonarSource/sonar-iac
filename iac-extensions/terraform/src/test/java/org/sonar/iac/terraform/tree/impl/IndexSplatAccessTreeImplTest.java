/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.IndexSplatAccessTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class IndexSplatAccessTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_index_splat_access() {
    IndexSplatAccessTree tree = parse("a[*]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.INDEX_SPLAT_ACCESS);
    assertThat(tree.children()).hasSize(4);
    assertThat(tree.subject()).isInstanceOfSatisfying(VariableExprTreeImpl.class, o -> assertThat(o.name()).isEqualTo("a"));
  }
}
