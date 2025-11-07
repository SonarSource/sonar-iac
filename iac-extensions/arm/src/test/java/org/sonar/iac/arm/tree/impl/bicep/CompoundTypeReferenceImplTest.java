/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.CompoundTypeReference;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

class CompoundTypeReferenceImplTest extends BicepTreeModelTest {
  @Test
  void shouldParseCompoundTypeReference() {
    SingularTypeExpression tree = parseBasic("basetype.subtype1.subtype2", BicepLexicalGrammar.TYPE_EXPRESSION);

    assertThat(tree.expression())
      .isInstanceOf(CompoundTypeReference.class)
      .returns(ArmTree.Kind.COMPOUND_TYPE_REFERENCE, from(ArmTree::getKind))
      .extracting(ArmTree::children, InstanceOfAssertFactories.list(ArmTree.class))
      .hasSize(2);

    var expression = (CompoundTypeReference) tree.expression();
    assertThat(expression.baseType())
      .isInstanceOf(CompoundTypeReference.class)
      .extracting(ArmTree::children, InstanceOfAssertFactories.list(ArmTree.class))
      .hasSize(2);
    assertThat(expression.suffix())
      .isInstanceOf(Identifier.class)
      .returns("subtype2", from(TextTree::value));
  }
}
