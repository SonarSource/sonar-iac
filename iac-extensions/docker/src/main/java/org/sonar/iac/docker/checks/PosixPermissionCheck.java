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
package org.sonar.iac.docker.checks;

import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.AddInstruction;
import org.sonar.iac.docker.tree.api.CopyInstruction;
import org.sonar.iac.docker.tree.api.TransferInstruction;
import org.sonar.iac.docker.utils.ArgumentUtils;

@Rule(key = "S2612")
public class PosixPermissionCheck implements IacCheck {

  private static final String MESSAGE = "Make sure this permission is safe.";
  private static final Pattern PERMISSION_FORMAT_CHECKER = Pattern.compile("[0-7]{3,4}");

  @Override
  public void initialize(InitContext init) {
    init.register(AddInstruction.class, PosixPermissionCheck::checkChmodPermission);
    init.register(CopyInstruction.class, PosixPermissionCheck::checkChmodPermission);
  }

  private static void checkChmodPermission(CheckContext ctx, TransferInstruction transferInstruction) {
    transferInstruction.options().stream()
      .filter(flag -> flag.name().equals("chmod"))
      .forEach(flag -> {
        if (isPermissionSensitive(ArgumentUtils.resolve(flag.value()).value())) {
          ctx.reportIssue(flag, MESSAGE);
        }
      });
  }

  private static boolean isPermissionSensitive(@Nullable String permission) {
    if (permission == null) {
      return false;
    }
    if (PERMISSION_FORMAT_CHECKER.matcher(permission).find()) {
      char lastDigit = permission.charAt(permission.length()-1);
      return isDigitRightSensitive(lastDigit);
    }
    return false;
  }

  /**
   * Check if the provided digit is sensitive : 'write' bit is set to 1 (rwx)
   * <a href="https://en.wikipedia.org/wiki/Chmod#:~:text=The%20chmod%20numerical%20format%20accepts,%2C%20setgid%2C%20and%20sticky%20flags">Wiki chmod</a>
   */
  private static boolean isDigitRightSensitive(char digit) {
    return ((digit - '0') & 0b10) > 0;
  }

}
