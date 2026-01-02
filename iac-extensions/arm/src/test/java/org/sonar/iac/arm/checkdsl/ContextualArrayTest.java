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
package org.sonar.iac.arm.checkdsl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.ObjectExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.CTX;
import static org.sonar.iac.arm.ArmTestUtils.parseObject;

class ContextualArrayTest {

  ContextualArray absent = ContextualArray.fromAbsent(CTX, "absentObject", null);
  ObjectExpression objectWithLists = parseObject("{\"emptyKey\": [], \"filledKey\": [{\"key\": \"value\"}] }");
  ContextualArray present = ContextualArray.fromPresent(CTX, (ArrayExpression) objectWithLists.properties().get(1).value(), "filledKey", null);
  ContextualArray empty = ContextualArray.fromPresent(CTX, (ArrayExpression) objectWithLists.properties().get(0).value(), "emptyKey", null);

  @Test
  void objects() {
    assertThat(present.objects()).hasSize(1);
    assertThat(absent.objects()).isEmpty();
    assertThat(empty.objects()).isEmpty();
  }
}
