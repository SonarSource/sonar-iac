/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
