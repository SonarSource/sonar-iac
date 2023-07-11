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
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ObjectExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseMinimumObjectExpression() {
    String code = code("{}\n");

    ObjectExpression tree = parse(code, BicepLexicalGrammar.OBJECT_EXPRESSION);
    assertThat(tree.properties()).isEmpty();
    assertThat(tree.is(ArmTree.Kind.OBJECT_EXPRESSION)).isTrue();

    SyntaxToken leftCurlyBrace = (SyntaxToken) tree.children().get(0);
    assertThat(leftCurlyBrace.value()).isEqualTo("{");

    SyntaxToken rightCurlyBrace = (SyntaxToken) tree.children().get(1);
    assertThat(rightCurlyBrace.value()).isEqualTo("}");
  }

  @Test
  void shouldParseObjectExpressionWithOneProperty() {
    String code = code("{\n key1: value1\n 'key2': value2\n}\n");

    ObjectExpression tree = parse(code, BicepLexicalGrammar.OBJECT_EXPRESSION);
    ArmAssertions.assertThat(tree)
      .containsKeyValue("key1", "value1")
      .containsKeyValue("key2", "value2")
      .hasSize(2)
      .hasRange(1, 0, 4, 1);
    assertThat(tree.is(ArmTree.Kind.OBJECT_EXPRESSION)).isTrue();

    SyntaxToken leftCurlyBrace = (SyntaxToken) tree.children().get(0);
    assertThat(leftCurlyBrace.value()).isEqualTo("{");

    Property property1 = (Property) tree.children().get(1);
    assertThat(((ArmTree) property1.key()).getKind()).isEqualTo(ArmTree.Kind.IDENTIFIER);
    assertThat(property1.key().value()).isEqualTo("key1");
    assertThat(((TextTree) property1.value()).value()).isEqualTo("value1");

    Property property2 = (Property) tree.children().get(2);
    assertThat(((ArmTree) property2.key()).getKind()).isEqualTo(ArmTree.Kind.STRING_COMPLETE);
    assertThat(property2.key().value()).isEqualTo("key2");
    assertThat(((TextTree) property2.value()).value()).isEqualTo("value2");

    SyntaxToken rightCurlyBrace = (SyntaxToken) tree.children().get(3);
    assertThat(rightCurlyBrace.value()).isEqualTo("}");
  }
}
