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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Variable;
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;

import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class MetadataDeclarationImplTest {
  BicepParser parser = BicepParser.create(BicepLexicalGrammar.METADATA_DECLARATION);

  @Test
  void shouldParseExpression() {
    assertThat(BicepLexicalGrammar.METADATA_DECLARATION)
      .matches("metadata identifier123=123")
      .matches("metadata identifier123 =123")
      .matches("metadata identifier123= 123")
      .matches("metadata identifier123 = 123")
      // defining a metadata of name the same as keyword is possible
      .matches("metadata func = 123")
      .matches("metadata for = 123")
      .matches("metadata metadata = 123")
      .matches("metadata param = 123")

      .notMatches("metadata identifier123")
      .notMatches("identifier123=123")
      .notMatches("metadata identifier123=.123");
  }

  @Test
  void shouldParseSimpleMetadataDeclaration() {
    String code = code("metadata identifier123=abc");

    MetadataDeclaration tree = (MetadataDeclaration) parser.parse(code, null);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.METADATA_DECLARATION)).isTrue();
    softly.assertThat(tree.declaratedName().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    softly.assertThat(tree.declaratedName().value()).isEqualTo("identifier123");
    softly.assertThat(tree.value().is(ArmTree.Kind.VARIABLE)).isTrue();
    softly.assertThat(((Variable) tree.value()).identifier())
      .extracting(e -> ((Identifier) e).value())
      .isEqualTo("abc");
    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("metadata", "identifier123", "=", "abc");
    softly.assertAll();
  }
}
