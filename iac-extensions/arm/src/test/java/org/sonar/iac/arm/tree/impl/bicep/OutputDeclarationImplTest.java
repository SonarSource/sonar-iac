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
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.bicep.HasDecorators;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

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
      .matches(code("@description('comment')", "@decorator()", "output myOutput resource 'myResource' = myValue"))

      .notMatches("output")
      .notMatches("output myOutput")
      .notMatches("output myOutput String")
      .notMatches("output myOutput String =")
      .notMatches("output myOutput resource 'myResource'")
      .notMatches("output myOutput resource 'myResource' =")
      .notMatches("OUTPUT myOutput String = myValue")
      .notMatches("outpute myOutput String = myValue")
      .notMatches("myOutput String = myValue");
  }

  @Test
  void shouldParseSimpleOutputDeclaration() {
    String code = code("@description('comment') output myOutput String = myValue");
    OutputDeclaration tree = parse(code, BicepLexicalGrammar.OUTPUT_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.declaratedName()).hasValue("myOutput").hasRange(1, 31, 1, 39);
    TextTree type = (TextTree) tree.type();
    assertThat(type.value()).isEqualTo("String");
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
    String code = code("output myOutput resource 'myResource' = myValue");
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
