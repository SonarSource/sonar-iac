/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.bicep.TestDeclaration;
import org.sonar.iac.common.api.tree.TextTree;

import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class TestDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseValidDeclarations() {
    ArmAssertions.assertThat(BicepLexicalGrammar.TEST_DECLARATION)
      .matches("test myTest 'path/to/test.bicep' = {}")
      .matches("test myTest 'path/to/test.bicep' = { name: 'foo' }")
      .matches("test myTest 'br:mcr.microsoft.com/bicep/test.bicep:bar' = {}")
      .matches("""
        test sample 'samples/sample1.bicep' = {
          params: {
            location: 'westus'
          }
        }""")
      // defining a test of name the same as keyword is possible
      .matches("test for 'test.bicep' = {}")
      .matches("test if 'test.bicep' = {}")
      .matches("test module 'test.bicep' = {}")
      .matches("test param 'test.bicep' = {}")
      .matches("test output 'test.bicep' = {}")

      .notMatches("test myTest = {}")
      .notMatches("test 'path/to/test.bicep' = {}")
      .notMatches("test myTest 'test.bicep' = 123")
      // no decorators on test declarations
      .notMatches("@decorator() test myTest 'test.bicep' = {}")
      .notMatches("test myTest =")
      .notMatches("test myTest")
      .notMatches("test 123")
      .notMatches("nottest myTest 'path/to/test.bicep' = {}")
      .notMatches("testing myTest 'path/to/test.bicep' = {}")
      .notMatches("test myTest 'path/to/test.bicep' {}")
      .notMatches("test myTest 'path/to/test.bicep' = if (bar) {}")
      .notMatches("test myTest 'path/to/test.bicep' = [for d in deployments: 'expression']");
  }

  @Test
  void shouldParseDeclarationCorrectly() {
    TestDeclaration tree = (TestDeclaration) createParser(BicepLexicalGrammar.TEST_DECLARATION).parse(
      """
        test stgTest '../storageAccount.bicep' = {
          name: 'storageDeploy'
        }""");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(TestDeclaration.class);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.TEST_DECLARATION);
    softly.assertThat(tree.children()).hasSize(5);
    softly.assertThat(tree.declaratedName().value()).isEqualTo("stgTest");
    softly.assertThat(tree.keyword().value()).isEqualTo("test");
    softly.assertThat(tree.body()).isInstanceOf(ObjectExpression.class);
    softly.assertThat(tree.type()).extracting(TextTree::value).isEqualTo("../storageAccount.bicep");
    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("test", "stgTest", "../storageAccount.bicep", "=", "{", "name", ":", "storageDeploy", "}");
    softly.assertAll();
  }
}
