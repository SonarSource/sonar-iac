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
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_attribute() {
    AttributeTree tree = parse("a = true", HclLexicalGrammar.ATTRIBUTE);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.ATTRIBUTE);
    assertThat(tree.key().value()).isEqualTo("a");
    assertThat(tree.equalSign().value()).isEqualTo("=");
    assertThat(tree.value()).isInstanceOfSatisfying(LiteralExprTree.class, a -> assertThat(a.value()).isEqualTo("true"));
  }
}
