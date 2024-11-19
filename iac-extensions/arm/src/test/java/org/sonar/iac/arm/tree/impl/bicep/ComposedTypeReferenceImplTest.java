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
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.ComposedTypeReference;
import org.sonar.iac.common.api.tree.TextTree;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class ComposedTypeReferenceImplTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.COMPOSED_TYPE_REFERENCE);

  @Test
  void shouldParseComposedIdentifier() {
    assertThat(BicepLexicalGrammar.COMPOSED_TYPE_REFERENCE)
      .matches("foo.objectProp.intProp")
      .matches("foo.objectProp")
      .matches("foo.objectProp.aaa.bbb")
      .matches("foo.objectProp._.bbb")
      .matches("foo.objectProp._value")
      .matches("foo.objectProp.Value")
      .matches("foo.objectProp.foo1")
      .matches("foo.objectProp.Bar9")
      .matches("foo.object_Prop")

      .notMatches("foo")
      .notMatches(".bar")
      .notMatches("foo..bar")
      .notMatches(".")
      .notMatches("foo.bar.")
      .notMatches("foo.bar.1");
  }

  @Test
  void shouldValidateComposedIdentifier() {
    var tree = (ComposedTypeReference) parser.parse("foo.objectProp.intProp", null);
    assertThat(tree.is(ArmTree.Kind.COMPOSED_TYPE_REFERENCE)).isTrue();
    assertThat(tree.identifiers().stream().map(TextTree::value).toList())
      .containsExactly("foo", "objectProp", "intProp");
    assertThat(tree.separators().stream().map(TextTree::value).toList())
      .containsExactly(".", ".");
    assertThat(tree.children()).hasSize(5);
    assertThat(tree.textRange()).hasRange(1, 0, 1, 22);
    assertThat(tree.identifiers().get(0)).hasRange(1, 0, 1, 3);
    assertThat(tree.separators().get(0)).hasRange(1, 3, 1, 4);
    assertThat(tree.identifiers().get(1)).hasRange(1, 4, 1, 14);
    assertThat(tree.separators().get(1)).hasRange(1, 14, 1, 15);
    assertThat(tree.identifiers().get(2)).hasRange(1, 15, 1, 22);
  }
}
