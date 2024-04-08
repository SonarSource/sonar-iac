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
      issue(60, 8, 60, 21),
      issue(61, 8, 61, 19),
      issue(62, 8, 62, 19),
      issue(63, 8, 63, 19),
      issue(79, 12, 79, 23),
      issue(84, 12, 84, 23),
      issue(100, 4, 102, 19),
      issue(104, 4, 106, 17),
      issue(108, 4, 110, 17),
      issue(112, 4, 114, 17),
      issue(143, 8, 143, 19),
      issue(154, 10, 154, 21),
      // TODO SONARIAC-1404 Fix AST nodes children/parents so that we can prevent issues from being raised in templates
      issue(91, 10, 91, 26),
      issue(92, 10, 92, 25),
      issue(93, 10, 93, 25),
      issue(94, 10, 94, 25)
    // TODO SONARIAC-1403 ARM Template parser should produce the same AST as Bicep for output with FOR loop
    // issue(159, 10, 159, 24),
    // issue(160, 10, 160, 22),
    // issue(161, 10, 161, 22),
    // issue(162, 10, 162, 22),
    // issue(173, 12, 173, 24),
    // issue(181, 12, 181, 24)
    );
  }

  @Test
  void testSourceAddressPrefixBicep() {
    BicepVerifier.verify("EmptyOrNullValueCheckTest/emptyOrNullValue.bicep", CHECK);
  }
}
