/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class TupleTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_tuple() {
    TupleTree tree = parse("[a, b]", HclLexicalGrammar.TUPLE);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.TUPLE);
    assertThat(tree.elements().trees()).hasSize(2);
    assertThat(tree.children()).hasSize(5);
  }
}
