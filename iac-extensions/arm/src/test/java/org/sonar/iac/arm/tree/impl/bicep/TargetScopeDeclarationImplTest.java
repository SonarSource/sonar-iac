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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.parser.utils.Assertions;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class TargetScopeDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseTargetScopeDeclaration() {
    Assertions.assertThat(BicepLexicalGrammar.TARGET_SCOPE_DECLARATION)
      .matches("targetScope=123")
      .matches("targetScope =123")
      // TODO SONARIAC-948 TargetScopeDeclaration should allow SPACING after the equals
      .notMatches("targetScope = 123")

      .notMatches("targetScope")
      .notMatches("targetScope=")
      .notMatches("TARGETscope=123")
      .notMatches("targetScope = ");
  }

  @Test
  void shouldParseSimpleTargetScopeDeclaration() {
    String code = code("targetScope=123");
    TargetScopeDeclaration tree = parse(code, BicepLexicalGrammar.TARGET_SCOPE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.TARGET_SCOPE_DECLARATION)).isTrue();
    assertThat(tree.scope()).isEqualTo(File.Scope.UNKNOWN);
    assertThat(tree.children()).map(token -> ((TextTree) token).value()).containsExactly("targetScope", "=", "123");
  }

  @ParameterizedTest
  @CsvSource(value = {
    "'managementGroup', MANAGEMENT_GROUP",
    "'resourceGroup',   RESOURCE_GROUP",
    "'subscription',    SUBSCRIPTION",
    "'tenant',          TENANT",
    "'other',           UNKNOWN"
  }, quoteCharacter = '\"')
  void shouldParseProperTarget(String targetScopeCode, String targetScopeEnum) {
    String code = code("targetScope=" + targetScopeCode);
    TargetScopeDeclaration tree = parse(code, BicepLexicalGrammar.TARGET_SCOPE_DECLARATION);
    assertThat(tree.scope()).isEqualTo(File.Scope.valueOf(targetScopeEnum));
  }
}
