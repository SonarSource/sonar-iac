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
package org.sonar.iac.arm.checkdsl;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.arm.ArmTestUtils.CTX;
import static org.sonar.iac.arm.ArmTestUtils.parseProperty;

class ContextualPropertyTest {

  ContextualProperty absent = ContextualProperty.fromAbsent(CTX, "absentProperty", null);
  Property property = parseProperty("\"key\": \"value\"");
  ContextualProperty present = ContextualProperty.fromPresent(CTX, property, null);

  @Test
  void reportIfOnPresentProperty() {
    present.reportIf(expression -> true, "message");
    verify(CTX, times(1)).reportIssue(property, "message", Collections.emptyList());
  }

  @Test
  void reportWithSecondaryLocation() {
    SecondaryLocation secondary = present.toSecondary("secondary");
    present.report("message", secondary);
    verify(CTX, times(1)).reportIssue(property, "message", List.of(secondary));
  }

  @Test
  void reportIfWhenPropertyIsAbsent() {
    absent.reportIf(expression -> true, "message");
    verify(CTX, never()).reportIssue(any(HasTextRange.class), anyString());
  }
}
