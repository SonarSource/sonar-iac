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

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.checks.utils.Chmod;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.TransferInstruction;

@Rule(key = "S2612")
public class PosixPermissionCheck implements IacCheck {

  private static final String MESSAGE = "Make sure this permission is safe.";

  @Override
  public void initialize(InitContext init) {
    init.register(TransferInstruction.class, PosixPermissionCheck::checkTransferChmodPermission);
    init.register(RunInstruction.class, PosixPermissionCheck::checkRunChmodPermission);
  }

  private static void checkRunChmodPermission(CheckContext ctx, RunInstruction runInstruction) {
    for (Chmod chmod : Chmod.extractChmodsFromArguments(runInstruction.arguments())) {
      if (chmod.hasPermission("o+w") || chmod.hasPermission("g+s") || chmod.hasPermission("u+s")) {
        TextRange textRange = TextRanges.merge(List.of(chmod.chmodArg.textRange(), chmod.permissionsArg.textRange()));
        ctx.reportIssue(textRange, MESSAGE);
      }
    }
  }

  private static void checkTransferChmodPermission(CheckContext ctx, TransferInstruction transferInstruction) {
    transferInstruction.options().stream()
      .filter(flag -> "chmod".equals(flag.name()))
      .filter(flag -> isPermissionSensitive(flag.value()))
      .forEach(flag -> ctx.reportIssue(flag, MESSAGE));
  }

  private static boolean isPermissionSensitive(@Nullable Argument permission) {
    if (permission == null) {
      return false;
    }
    String permissionString = ArgumentResolution.of(permission).value();
    if (permissionString == null) {
      return false;
    }
    return Chmod.Permission.fromNumeric(permissionString).hasRight("o+w");
  }
}
