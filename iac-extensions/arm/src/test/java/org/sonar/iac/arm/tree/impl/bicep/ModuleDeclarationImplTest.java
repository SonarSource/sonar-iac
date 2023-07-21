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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.ModuleDeclaration;
import org.sonar.iac.common.testing.IacCommonAssertions;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.sonar.iac.common.testing.IacTestUtils.code;

class ModuleDeclarationImplTest extends BicepTreeModelTest {
  @Test
  void shouldParseValidDeclarations() {
    ArmAssertions.assertThat(BicepLexicalGrammar.MODULE_DECLARATION)
      .matches("module foo 'path-to-file' = {}")
      .matches("module foo 'path-to-file' = if (bar) {}")
      .matches("module foo 'path-to-file' = [for d in deployments: 'expression']")
      .matches("module foo 'br:mcr.microsoft.com/bicep/foo.bicep:bar' = {}")
      .matches("@batchSize(4) module foo 'br:mcr.microsoft.com/bicep/foo.bicep:bar' = {}")
      .matches("@sys.batchSize(4) module foo 'br:mcr.microsoft.com/bicep/foo.bicep:bar' = {}")
      .matches(code("@sys.batchSize(4)", "@decorator()", "module foo 'br:mcr.microsoft.com/bicep/foo.bicep:bar' = {}"))
      // defining a module of name the same as keyword is possible
      .matches("module for 'resource.bicep' = { name: 'foo' }")
      .matches("module if 'resource.bicep' = { name: 'foo' }")
      .matches("module func 'resource.bicep' = { name: 'foo' }")
      .matches("module metadata 'resource.bicep' = { name: 'foo' }")
      .matches("module param 'resource.bicep' = { name: 'foo' }")
      .matches("module output 'resource.bicep' = { name: 'foo' }")

      .notMatches("module foo = {}")
      .notMatches("module 'br:mcr.microsoft.com/bicep/foo.bicep:bar' = {}")
      .notMatches("module foo bar = {}")
      .notMatches("module foo 'foo.bicep' = 123");
  }

  @Test
  void shouldParseDeclarationCorrectly() {
    ModuleDeclaration tree = (ModuleDeclaration) createParser(BicepLexicalGrammar.MODULE_DECLARATION).parse(
      code("@batchSize(4) ",
        "module stgModule '../storageAccount.bicep' = {",
        "  name: 'storageDeploy'",
        "}"));

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(ModuleDeclaration.class);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.MODULE_DECLARATION);
    softly.assertThat(tree.decorators()).hasSize(1);
    softly.assertThat(tree.children()).hasSize(6);
    softly.assertThat(tree.children().get(5)).isInstanceOf(Expression.class);
    softly.assertThat(tree.name().value()).isEqualTo("stgModule");
    IacCommonAssertions.assertThat(tree.type().textRange()).hasRange(2, 17, 2, 42);
    IacCommonAssertions.assertThat(tree.value().textRange()).hasRange(2, 45, 4, 1);
    softly.assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("@", "batchSize", "(", "4", ")", "module", "stgModule", "../storageAccount.bicep", "=", "{", "name", ":", "storageDeploy", "}");
    softly.assertAll();
  }
}
