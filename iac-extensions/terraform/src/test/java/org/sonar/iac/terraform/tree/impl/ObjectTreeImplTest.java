/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
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
