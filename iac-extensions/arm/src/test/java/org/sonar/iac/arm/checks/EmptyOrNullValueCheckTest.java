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
package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class EmptyOrNullValueCheckTest {
  private static final EmptyOrNullValueCheck CHECK = new EmptyOrNullValueCheck();

  @Test
  void testEmptyOrNullValueJson() {
    ArmVerifier.verify("EmptyOrNullValueCheckTest/emptyOrNullValue.json", CHECK,
      issue(6, 4, 6, 16, "Remove this null variable or complete with real code."),
      issue(7, 4, 7, 14, "Remove this empty string or complete with real code."),
      issue(8, 4, 8, 14, "Remove this empty object or complete with real code."),
      issue(9, 4, 9, 14, "Remove this empty array or complete with real code."),
      issue(21, 8, 21, 19, "Remove this null property or complete with real code."),
      issue(56, 6, 56, 19, "Remove this null property or complete with real code."),
      issue(57, 6, 57, 16, "Remove this empty string or complete with real code."),
      issue(58, 6, 58, 16, "Remove this empty object or complete with real code."),
      issue(59, 6, 59, 17, "Remove this empty array or complete with real code."),
      issue(61, 8, 61, 21),
      issue(62, 8, 62, 19),
      issue(63, 8, 63, 19),
      issue(64, 8, 64, 19),
      issue(80, 12, 80, 23),
      issue(85, 12, 85, 23),
      issue(92, 10, 92, 26),
      issue(93, 10, 93, 25),
      issue(94, 10, 94, 25),
      issue(95, 10, 95, 25),
      issue(101, 4, 103, 19),
      issue(105, 4, 107, 17),
      issue(109, 4, 111, 17),
      issue(113, 4, 115, 17),
      issue(144, 8, 144, 19),
      issue(155, 10, 155, 21)
    // TODO SONARIAC-1403 ARM Template parser should produce the same AST as Bicep for output with FOR loop
    // issue(169, 10, 169, 24),
    // issue(170, 10, 170, 22),
    // issue(171, 10, 171, 22),
    // issue(172, 10, 172, 22),
    // issue(182, 10, 183, 24),
    // issue(191, 15, 191, 26)
    );
  }

  @Test
  void testEmptyOrNullValueBicep() {
    BicepVerifier.verify("EmptyOrNullValueCheckTest/emptyOrNullValue.bicep", CHECK);
  }
}
