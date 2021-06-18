/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_object() {
    ObjectTree tree = parse("{a: 1, b: 2}", HclLexicalGrammar.OBJECT);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.OBJECT);
    assertThat(tree.elements().trees()).hasSize(2);
  }

  @Test
  void newline_separated_elements() {
    ObjectTree tree = parse("{a: 1\n b: 2}", HclLexicalGrammar.OBJECT);
    assertThat(tree).isInstanceOfSatisfying(ObjectTreeImpl.class, o -> assertThat(o.elements().trees()).hasSize(2));
  }
}
