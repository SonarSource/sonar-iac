/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.WildcardTypeReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class WildcardTypeReferenceImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseIdentifierWildcardType() {
    String code = "anObject.*";
    WildcardTypeReference tree = parse(code, BicepLexicalGrammar.TYPE_REFERENCE);
    assertThat(tree.is(ArmTree.Kind.WILDCARD_TYPE_REFERENCE)).isTrue();
    assertThat(tree.getType().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    assertThat(((Identifier) tree.getType()).value()).isEqualTo("anObject");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("anObject", ".", "*");
  }

  @Test
  void shouldParseAmbientTypeWildcardType() {
    String code = "string.*";
    WildcardTypeReference tree = parse(code, BicepLexicalGrammar.TYPE_REFERENCE);
    assertThat(tree.is(ArmTree.Kind.WILDCARD_TYPE_REFERENCE)).isTrue();
    assertThat(tree.getType().is(ArmTree.Kind.AMBIENT_TYPE_REFERENCE)).isTrue();
    assertThat(((AmbientTypeReference) tree.getType()).value()).isEqualTo("string");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("string", ".", "*");
  }

  @Test
  void shouldConvertToString() {
    WildcardTypeReference tree = parse("anObject.*", BicepLexicalGrammar.TYPE_REFERENCE);
    assertThat(tree).hasToString("anObject.*");
  }
}
