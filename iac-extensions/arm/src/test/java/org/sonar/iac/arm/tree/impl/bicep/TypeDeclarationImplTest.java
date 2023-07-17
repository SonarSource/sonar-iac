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
import org.sonar.iac.arm.tree.api.bicep.TypeDeclaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class TypeDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseTypeDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.TYPE_DECLARATION)
      .matches("type myType=abc")
      .matches("type myType= abc")
      .matches("type myType =abc")
      .matches("type myType = abc")
      .matches("type myType = bool[] | int?")
      .matches("@description('my type') type myType = abc")
      .matches("@sys.description('my type') type myType = abc")
      .matches("@sys.description('my type') type myType = bool[] | int?")
      .matches(code("@description('my type')", "@decorator()", "type myType = abc"))

      .notMatches("type myType")
      .notMatches("type myType=")
      .notMatches("myType=abc")
      .notMatches("type")
      .notMatches("type = abc");
  }

  @Test
  void shouldParseSimpleTypeDeclaration() {
    String code = code("@description('my type') type myType = abc");
    TypeDeclaration tree = parse(code, BicepLexicalGrammar.TYPE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.TYPE_DECLARATION)).isTrue();
    assertThat(tree.name().value()).isEqualTo("myType");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.type())).containsExactly("abc");
    assertThat(tree.decorators()).hasSize(1);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("@", "description", "(", "my type", ")", "type", "myType", "=", "abc");
  }
}
