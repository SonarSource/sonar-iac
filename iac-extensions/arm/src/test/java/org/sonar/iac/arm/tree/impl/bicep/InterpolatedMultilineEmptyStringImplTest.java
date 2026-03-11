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
import org.sonar.iac.arm.tree.api.ArmTree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class InterpolatedMultilineEmptyStringImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseEmptyString() {
    InterpolatedMultilineEmptyStringImpl tree = parse("$''''''", BicepLexicalGrammar.INTERPOLATED_MULTILINE_STRING);

    assertThat(tree.value()).isEmpty();
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.children().get(0)).hasToString("$'''");
    assertThat(tree.children().get(1)).hasToString("'''");
    assertThat(tree.getKind()).isEqualTo(Kind.INTERPOLATED_MULTILINE_STRING);
  }
}
