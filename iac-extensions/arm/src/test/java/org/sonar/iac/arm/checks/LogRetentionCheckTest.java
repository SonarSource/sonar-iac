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
package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.arm.checks.ArmVerifier.verify;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class LogRetentionCheckTest {

  IacCheck check = new LogRetentionCheck();

  @Test
  void testLogRetention() {
    verify("LogRetentionCheck/test.json", check,
      issue(range(12, 10, 12, 28), "Make sure that defining a short log retention duration is safe here."),
      issue(range(21, 20, 23, 9), "Omitting \"retentionDays\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(31, 20, 33, 9), "Omitting \"isEnabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(42, 10, 42, 28), "Omitting \"isEnabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(43, 10, 43, 28), "Make sure that defining a short log retention duration is safe here."),
      issue(range(53, 10, 53, 28), "Omitting \"isEnabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(60, 14, 60, 50), "Omitting \"insights\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(71, 10, 71, 19), "Make sure that defining a short log retention duration is safe here."),
      issue(range(81, 27, 83, 9), "Omitting \"days\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(91, 27, 93, 9), "Omitting \"enabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(102, 10, 102, 19), "Make sure that defining a short log retention duration is safe here."),
      issue(range(103, 10, 103, 26), "Omitting \"enabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(114, 10, 114, 26), "Omitting \"enabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(120, 14, 120, 58), "Omitting \"retentionPolicy\" results in a short log retention duration. Make sure it is safe here."));
  }
}
