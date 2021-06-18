/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class LabelTreeImplTest extends TerraformTreeModelTest {

  @Test
  void string_literal() {
    LabelTree tree = parse("\"a\"", HclLexicalGrammar.LABEL);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.LABEL);
    assertThat(tree.value()).isEqualTo("\"a\"");
    assertThat(tree.value()).isEqualTo(tree.token().value());
  }

  @Test
  void identifier() {
    LabelTree tree = parse("id", HclLexicalGrammar.LABEL);
    assertThat(tree.value()).isEqualTo("id");
    assertThat(tree.value()).isEqualTo(tree.token().value());
  }
}
