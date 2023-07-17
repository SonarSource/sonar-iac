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
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.ObjectType;
import org.sonar.iac.arm.tree.api.bicep.ObjectTypeProperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ObjectTypeImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseObjectType() {
    ArmAssertions.assertThat(BicepLexicalGrammar.OBJECT_TYPE)
      .matches("{}")
      .matches("{ }")
      .matches("{\n}")
      .matches("{ identifier : abc }")
      .matches("{\nidentifier : abc\n}")
      .matches("{\n'string complete' : abc\n}")
      .matches("{\n'''single multiline''' : abc\n}")
      .matches("{\n'''\nsingle\nmultiline\n''' : abc\n}")
      .matches("{*: abc}")
      .matches("{\n*: abc\n}")
      // it is invalid bicep syntax but accepted by this parser
      .matches("{* : abc\n}")

      .notMatches("identifier :")
      .notMatches("output myOutput : abc")
      .notMatches("foo bar : baz")
      .notMatches("foo 'bar' : baz")
      .notMatches("foo '''bar''' : baz")
      .notMatches("identifier = abc")
      .notMatches("identifier = {}");
  }

  @Test
  void shouldParseSimpleObjectType() {
    ObjectType tree = parse(code("{ identifier : abc }"), BicepLexicalGrammar.OBJECT_TYPE);

    assertThat(tree.is(ArmTree.Kind.OBJECT_TYPE)).isTrue();
    ObjectTypeProperty property = (ObjectTypeProperty) tree.properties().get(0);
    assertThat(property.name()).isInstanceOf(Identifier.class);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(property.typeExpression()))
      .containsExactly("abc");
  }
}
