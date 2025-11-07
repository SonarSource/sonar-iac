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
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.bicep.HasDecorators;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class OutputDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseOutputDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.OUTPUT_DECLARATION)
      .matches("output myOutput string=myValue")
      .matches("output myOutput string = myValue")
      .matches("output myOutput bool = 5 <= 3")
      .matches("output myOutput resource 'myResource'=myValue")
      .matches("output myOutput resource 'myResource' = myValue")
      // defining an output of name the same as keyword is possible
      .matches("output type string = myValue")
      .matches("output if string = myValue")
      .matches("output for string = myValue")
      .matches("output myOutput string = virtualNetwork::subnet1.id")
      .matches("@description('comment') output myOutput String = myValue")
      .matches("@description('comment') output myOutput resource 'myResource' = myValue")
      .matches("@sys.description('comment') output myOutput resource 'myResource' = myValue")
      .matches("""
        @description('comment')
        @decorator()
        output myOutput resource 'myResource' = myValue""")
      .matches("output out1 stringArrayType[*] = 'bar'")
      .matches("output out2 stringArrayType[*]? = 'bar'")
      .matches("output out3 stringArrayType[*][] = ['bar']")
      .matches("output out4 stringArrayType[*][]? = ['bar']")
      .matches("output out5 basket.*[*][0] = 'apple'")
      .matches("output out6 basket.*[*][] = []")
      .matches("output out7 basket.*[*] = ['apple', 1]")
      .matches("output out8 basket.*[] = []")
      .matches("output out9 basket.* = []")
      .matches("output outA basket.*[*][0]? = 'apple'")
      .matches("output outB basket.*[*][]? = []")
      .matches("output outC basket.*[*]? = ['apple', 1]")
      .matches("output outD basket.*[]? = []")
      .matches("output outE basket.*? = []")
      .matches("output outF (basket.*[*][0])[] = []")
      .matches("output outG (basket.*[*][])[] = []")
      .matches("output outH (basket.*[*])[] = []")
      .matches("output outI (basket.*[])[] = []")
      .matches("output outJ (basket.*)[] = []")
      .matches("output outK (basket.*[*][0]?)[] = []")
      .matches("output outL (basket.*[*][]?)[] = []")
      .matches("output outM (basket.*[*]?)[] = []")
      .matches("output outN (basket.*[]?)[] = []")
      .matches("output outO (basket.*)[] = []")

      .notMatches("output")
      .notMatches("output myOutput")
      .notMatches("output myOutput String")
      .notMatches("output myOutput String =")
      .notMatches("output myOutput resource 'myResource'")
      .notMatches("output myOutput resource 'myResource' =")
      .notMatches("OUTPUT myOutput String = myValue")
      .notMatches("outpute myOutput String = myValue")
      .notMatches("myOutput String = myValue")
      // The ? should be last
      .notMatches("output out5 stringArrayType[*]?[] = ['bar']")
      .notMatches("output out6 stringArrayType?[*][] = ['bar']")
      .notMatches("output out7 stringArrayType?[*] = ['bar']")
      .notMatches("output out8 stringArrayType?[] = ['bar']");
  }

  @Test
  void shouldParseSimpleOutputDeclaration() {
    var code = "@description('comment') output myOutput String = myValue";
    var tree = (OutputDeclaration) parse(code, BicepLexicalGrammar.OUTPUT_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.declaratedName()).hasValue("myOutput").hasRange(1, 31, 1, 39);
    var type = (SingularTypeExpression) tree.type();
    assertThat(((TextTree) type.expression()).value()).isEqualTo("String");
    assertThat(type.questionMark()).isNull();
    assertThat(type.textRange()).hasRange(1, 40, 1, 46);
    assertThat(tree.value()).asWrappedIdentifier().hasValue("myValue").hasRange(1, 49, 1, 56);
    assertThat(tree.condition()).isNull();
    assertThat(tree.copyCount()).isNull();
    assertThat(tree.copyInput()).isNull();
    assertThat(((HasDecorators) tree).decorators()).hasSize(1);
    assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("@", "description", "(", "comment", ")", "output", "myOutput", "String", "=", "myValue");
  }

  @Test
  void shouldParseSimpleOutputDeclarationWithResource() {
    var code = "output myOutput resource 'myResource' = myValue";
    OutputDeclaration tree = parse(code, BicepLexicalGrammar.OUTPUT_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.declaratedName()).hasValue("myOutput").hasRange(1, 7, 1, 15);
    assertThat(((TextTree) tree.type()).value()).isEqualTo("myResource");
    assertThat(tree.type().textRange()).hasRange(1, 25, 1, 37);
    assertThat(tree.value()).asWrappedIdentifier().hasValue("myValue").hasRange(1, 40, 1, 47);
    assertThat(tree.condition()).isNull();
    assertThat(tree.copyCount()).isNull();
    assertThat(tree.copyInput()).isNull();
  }
}
