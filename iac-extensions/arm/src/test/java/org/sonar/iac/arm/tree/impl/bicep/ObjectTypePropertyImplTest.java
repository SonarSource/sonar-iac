/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.arm.tree.api.bicep.ObjectTypeProperty;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.checks.TextUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ObjectTypePropertyImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseObjectTypeProperty() {
    ArmAssertions.assertThat(BicepLexicalGrammar.OBJECT_TYPE_PROPERTY)
      .matches("identifier:abc")
      .matches("identifier: abc")
      .matches("identifier : abc")
      .matches("'string complete' : abc")
      .matches("'''single multiline''' : abc")
      .matches("'''\nsingle\nmultiline\n''' : abc")
      .matches("@minLength(10) identifier:abc")
      .matches("@sys.minLength(10) identifier:abc")
      .matches(code("@sys.minLength(10)", "@decorator()", "identifier:abc"))
      .matches("identifier: ( array )")
      .matches("identifier: ( bool | int )")

      .notMatches("identifier :")
      .notMatches("output myOutput : abc")
      .notMatches("foo bar : baz")
      .notMatches("foo 'bar' : baz")
      .notMatches("foo '''bar''' : baz")
      .notMatches("identifier = abc")
      .notMatches("identifier = {}");
  }

  @Test
  void shouldParseSimpleObjectTypeProperty() {
    String code = code("@minLength(10) identifier : abc");

    ObjectTypeProperty tree = parse(code, BicepLexicalGrammar.OBJECT_TYPE_PROPERTY);

    assertThat(tree.is(ArmTree.Kind.OBJECT_TYPE_PROPERTY)).isTrue();
    assertThat(tree.name())
      .isInstanceOf(IdentifierImpl.class);
    assertThat(TextUtils.getValue(tree.name())).contains("identifier");
    ArmAssertions.assertThat(tree.textRange()).hasRange(1, 0, 1, 31);

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.typeExpression()))
      .containsExactly("abc");

    assertThat(tree.decorators()).hasSize(1);

    Identifier token1 = (Identifier) tree.children().get(1);
    assertThat(token1.value()).isEqualTo("identifier");

    SyntaxToken token2 = (SyntaxToken) tree.children().get(2);
    assertThat(token2.value()).isEqualTo(":");

    SingularTypeExpression token3 = (SingularTypeExpression) tree.children().get(3);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(token3))
      .containsExactly("abc");

    assertThat(tree.children()).hasSize(4);
  }
}
