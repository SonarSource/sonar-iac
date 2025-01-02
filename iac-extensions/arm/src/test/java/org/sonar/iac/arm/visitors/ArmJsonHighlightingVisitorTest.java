/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.ArmJsonParser;
import org.sonar.iac.common.testing.AbstractHighlightingTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ArmJsonHighlightingVisitorTest extends AbstractHighlightingTest {

  protected ArmJsonHighlightingVisitorTest() {
    super(new ArmHighlightingVisitor(), new ArmJsonParser());
  }

  @Test
  void shouldNotThrowExceptionWhenHighlightingResourceDeclaration() {
    String code = code("{",
      "  \"resources\": [",
      "    {",
      "      \"type\": \"Microsoft.Kusto/clusters\",",
      "      \"apiVersion\": \"2022-12-29\",",
      "      \"name\": \"myResource\"",
      "    }",
      "  ]",
      "}");
    assertThatNoException().isThrownBy(() -> highlight(code));
  }
}
