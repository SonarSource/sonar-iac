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
package org.sonar.iac.arm.checkdsl;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.arm.ArmTestUtils.CTX;
import static org.sonar.iac.arm.ArmTestUtils.parseParameter;
import static org.sonar.iac.arm.ArmTestUtils.parseProperty;

class ContextualParameterTest {

  ParameterDeclaration parameter = parseParameter("myParam", "string", "myValue");
  ParameterDeclaration parameterNoDefaultValue = parseParameter("myParam", "string", null);
  ContextualParameter present = ContextualParameter.fromPresent(CTX, parameter, null);
  ContextualParameter presentNoDefaultValue = ContextualParameter.fromPresent(CTX, parameterNoDefaultValue, null);

  @Test
  void reportOnPresentParameterNoDefaultValue() {
    presentNoDefaultValue.report("message");
    verify(CTX, times(1)).reportIssue(parameterNoDefaultValue, "message", Collections.emptyList());
  }

  @Test
  void noReportIfOnPresentParameterWithNoDefaultValut() {
    presentNoDefaultValue.reportIf(expression -> true, "message");
    verify(CTX, never()).reportIssue(parameterNoDefaultValue, "message", Collections.emptyList());
  }

  @Test
  void reportWithSecondaryLocation() {
    SecondaryLocation secondary = present.toSecondary("secondary");
    present.report("message", secondary);
    verify(CTX, times(1)).reportIssue(parameter.defaultValue(), "message", List.of(secondary));
  }
}
