/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class ObjectExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseValidObjectExpressions() {
    ArmAssertions.assertThat(BicepLexicalGrammar.OBJECT_EXPRESSION)
      .matches("{}")
      .matches("{ key1: value1 }")
      .matches("{ key1: value1, key2: value2 }")
      .matches("{ key1: value1, key2: value2, }")
      .matches("""
        {
          key1: value1
          key2: value2
        }""")
      .matches("""
        {
          key1: value1, key2: value2
          key3: value3
        }""")
      .matches("""
        {
          key1: value1,
          key2: value2
        }""")

      .notMatches("{ , key1: value1 }");
  }

  @Test
  void shouldParseMinimumObjectExpression() {
    String code = "{}\n";

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
    String code = """
      {
        key1: value1
        'key2': value2
      }
      """;

    ObjectExpression tree = parse(code, BicepLexicalGrammar.OBJECT_EXPRESSION);
    assertThat(tree)
      .containsVariableKeyValue("key1", "value1")
      .containsVariableKeyValue("key2", "value2")
      .hasSize(2)
      .hasRange(1, 0, 4, 1);
    assertThat(tree.is(ArmTree.Kind.OBJECT_EXPRESSION)).isTrue();

    SyntaxToken leftCurlyBrace = (SyntaxToken) tree.children().get(0);
    assertThat(leftCurlyBrace.value()).isEqualTo("{");

    Property property1 = (Property) tree.children().get(1);
    assertThat(((ArmTree) property1.key()).getKind()).isEqualTo(ArmTree.Kind.IDENTIFIER);
    assertThat(property1.key().value()).isEqualTo("key1");
    HasIdentifier value1 = (HasIdentifier) property1.value();
    assertThat(((TextTree) value1.identifier()).value()).isEqualTo("value1");

    Property property2 = (Property) tree.children().get(2);
    assertThat(((ArmTree) property2.key()).getKind()).isEqualTo(ArmTree.Kind.STRING_LITERAL);
    assertThat(property2.key().value()).isEqualTo("key2");
    HasIdentifier value2 = (HasIdentifier) property2.value();
    assertThat(((TextTree) value2.identifier()).value()).isEqualTo("value2");

    SyntaxToken rightCurlyBrace = (SyntaxToken) tree.children().get(3);
    assertThat(rightCurlyBrace.value()).isEqualTo("}");
  }

  @Test
  void shouldParseObjectExpressionWithNestedResource() {
    String code = """
      {
        key1: value1
        resource subnet1 'subnets' = {
          name: 'subnet1Name'
        }
        key2: value2
        resource subnet2 'subnets' = {
          name: 'subnet2Name'
        }
      }""";
    ObjectExpression tree = parse(code, BicepLexicalGrammar.OBJECT_EXPRESSION);

    assertThat(tree).hasRange(1, 0, 10, 1);
    assertThat(tree.properties()).hasSize(2);
    assertThat(tree.nestedResources()).hasSize(2);

    Property property1 = (Property) tree.properties().get(0);
    assertThat(property1.key().value()).isEqualTo("key1");
    assertThat(property1.value()).asWrappedIdentifier().hasValue("value1");
    Property property2 = (Property) tree.properties().get(1);
    assertThat(property2.key().value()).isEqualTo("key2");
    assertThat(property2.value()).asWrappedIdentifier().hasValue("value2");

    ResourceDeclaration resource1 = tree.nestedResources().get(0);
    assertThat(((StringLiteral) resource1.name())).hasValue("subnet1Name");
    ResourceDeclaration resource2 = tree.nestedResources().get(1);
    assertThat(((StringLiteral) resource2.name())).hasValue("subnet2Name");

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("{", "key1", ":", "value1", "resource", "subnet1", "subnets",
      "=", "{", "name", ":", "subnet1Name", "}", "key2", ":", "value2", "resource", "subnet2", "subnets", "=", "{", "name",
      ":", "subnet2Name", "}", "}");
  }

  @Test
  void shouldConvertToString() {
    String code = """
      {
        key1: 'value1'
        key2: 'value2'
      }""";
    ObjectExpression objectExpression = parse(code, BicepLexicalGrammar.OBJECT_EXPRESSION);
    assertThat(objectExpression).hasToString("{key1: 'value1', key2: 'value2'}");
  }

  @Test
  void shouldParseCommaSeparatedObject() {
    var code = "{ name: 'web', network: '127.0.0.1' }";

    ObjectExpression tree = parse(code, BicepLexicalGrammar.OBJECT_EXPRESSION);

    assertThat(tree.properties()).hasSize(2);
    assertThat(tree.children()).hasSize(5);
  }

  @Test
  void shouldParseObjectWithMixedSeparators() {
    var code = """
      { name: 'web', network: '127.0.0.1'
      port: 80 }""";

    ObjectExpression tree = parse(code, BicepLexicalGrammar.OBJECT_EXPRESSION);

    assertThat(tree.properties()).hasSize(3);
    assertThat(tree.children()).hasSize(6);
  }
}
