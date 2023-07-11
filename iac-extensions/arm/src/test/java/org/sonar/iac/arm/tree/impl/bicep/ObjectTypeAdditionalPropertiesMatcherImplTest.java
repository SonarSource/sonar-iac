/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.ObjectTypeAdditionalPropertiesMatcher;
import org.sonar.iac.arm.tree.api.bicep.ObjectTypeProperty;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.checks.TextUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ObjectTypeAdditionalPropertiesMatcherImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseObjectTypeAdditionalPropertiesMatcher() {
    ArmAssertions.assertThat(BicepLexicalGrammar.OBJECT_TYPE_ADDITIONAL_PROPERTIES_MATCHER)
      .matches("*: abc")
      .matches("*:abc")

      .notMatches("* :abc")
      .notMatches("* : abc")
      .notMatches("identifier :")
      .notMatches("output myOutput : abc");
  }

  @Test
  void shouldParseSimpleObjectTypeAdditionalPropertiesMatcher() {
    String code = code("*: abc");

    ObjectTypeAdditionalPropertiesMatcher tree = parse(code, BicepLexicalGrammar.OBJECT_TYPE_ADDITIONAL_PROPERTIES_MATCHER);

    assertThat(tree.is(ArmTree.Kind.OBJECT_TYPE_ADDITIONAL_PROPERTIES_MATCHER)).isTrue();

    assertThat(tree.typeExpression().value()).isEqualTo("abc");

    SyntaxToken token1 = (SyntaxToken) tree.children().get(0);
    assertThat(token1.value()).isEqualTo("*:");

    StringLiteral token3 = (StringLiteral) tree.children().get(1);
    assertThat(token3.value()).isEqualTo("abc");

    assertThat(tree.children()).hasSize(2);
  }
}
