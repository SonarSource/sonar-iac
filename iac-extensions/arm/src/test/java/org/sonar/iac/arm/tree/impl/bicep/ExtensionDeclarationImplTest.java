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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.ExtensionDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class ExtensionDeclarationImplTest extends BicepTreeModelTest {
  @Test
  void shouldParseValidStatement() {
    ArmAssertions.assertThat(BicepLexicalGrammar.EXTENSION_DECLARATION)
      .matches("extension 'foo'")
      .matches("extension foo")
      .matches("extension 'foo' as bar")
      .matches("extension 'foo' with {}")
      .matches("extension 'foo' with {} as bar")
      .matches("@decorator('parameter') extension 'foo' with {} as bar")
      .matches("@sys.decorator('parameter') extension 'foo' with {} as bar")
      .matches("""
        @sys.decorator('parameter')
        @decorator()
        extension 'foo' with {} as bar""")
      // defining an extension as of name the same as keyword is possible
      .matches("extension 'kubernetes@1.0.0' with {namespace: 'default'} as if")
      .matches("extension 'kubernetes@1.0.0' with {namespace: 'default'} as type")
      .matches("extension 'kubernetes@1.0.0' with {namespace: 'default'} as var")
      .matches("extension 'kubernetes@1.0.0' with {namespace: 'default'} as param")
      .matches("extension 'kubernetes@1.0.0' with {namespace: 'default'} as for")
      .matches("extension 'kubernetes@1.0.0' with {namespace: 'default'} as func")

      .notMatches("extension")
      .notMatches("extension with {}")
      .notMatches("extension as bar")
      .notMatches("extension kubernetes@1.0.0")
      .notMatches("@decorator('parameter') extension")
      .notMatches("extension 'foo' as bar with")
      .notMatches("extension 'foo' with as bar")
      .notMatches("extension 'foo' as");
  }

  @Test
  void shouldParseExtensionStatementWithInterpolationStringAsSpecification() {
    ExtensionDeclaration tree = (ExtensionDeclaration) createParser(BicepLexicalGrammar.EXTENSION_DECLARATION)
      .parse("@decorator('parameter') extension 'kubernetes@1.0.0' with { 'key':'value' } as k8s");

    assertThat(tree).isInstanceOf(ExtensionDeclaration.class);
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.EXTENSION_DECLARATION);
    assertThat(tree.decorators()).isNotNull().hasSize(1);

    assertThat(tree.children()).hasSize(5);
    assertThat(tree.children().get(0)).isSameAs(tree.decorators().get(0));
    assertThat(tree.children().get(1)).isSameAs(tree.keyword());
    assertThat(tree.children().get(2)).isSameAs(tree.specification());
    assertThat(tree.children().get(3)).isSameAs(tree.withClause());
    assertThat(tree.children().get(4)).isSameAs(tree.asClause());

    assertThat(tree.specification().getKind()).isEqualTo(ArmTree.Kind.STRING_LITERAL);
    var interpolatedString = (StringLiteral) tree.specification();
    assertThat(interpolatedString.value()).isEqualTo("kubernetes@1.0.0");
    assertThat(tree.keyword().value()).isEqualTo("extension");
    var properties = tree.withClause().object().allPropertiesFlattened().toList();
    assertThat(properties).hasSize(1);
    assertThat(tree.asClause().alias().value()).isEqualTo("k8s");
  }

  @Test
  void shouldParseExtensionStatementWithIdentifierAsSpecification() {
    ExtensionDeclaration tree = (ExtensionDeclaration) createParser(BicepLexicalGrammar.EXTENSION_DECLARATION)
      .parse("extension microsoftGraphV1_0");

    assertThat(tree).isInstanceOf(ExtensionDeclaration.class);
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.EXTENSION_DECLARATION);
    assertThat(tree.decorators()).isNotNull().isEmpty();

    assertThat(tree.specification().getKind()).isEqualTo(ArmTree.Kind.IDENTIFIER);
    var interpolatedString = (Identifier) tree.specification();
    assertThat(interpolatedString.value()).isEqualTo("microsoftGraphV1_0");
    assertThat(tree.keyword().value()).isEqualTo("extension");
    assertThat(tree.withClause()).isNull();
    assertThat(tree.asClause()).isNull();
  }

  @Test
  void shouldParseMinimalExtensionStatement() {
    ExtensionDeclaration tree = (ExtensionDeclaration) createParser(BicepLexicalGrammar.EXTENSION_DECLARATION)
      .parse("extension microsoftGraphV1_0");

    assertThat(tree).isInstanceOf(ExtensionDeclaration.class);
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.EXTENSION_DECLARATION);
    assertThat(tree.decorators()).isNotNull().isEmpty();

    assertThat(tree.children()).hasSize(2);
    assertThat(tree.children().get(0)).isInstanceOf(SyntaxToken.class);
    assertThat(tree.children().get(1)).isSameAs(tree.specification());

    assertThat(tree.specification().getKind()).isEqualTo(ArmTree.Kind.IDENTIFIER);
    assertThat(((Identifier) tree.specification()).value()).isEqualTo("microsoftGraphV1_0");
    assertThat(tree.withClause()).isNull();
    assertThat(tree.asClause()).isNull();
  }
}
