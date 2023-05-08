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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.Chmod;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.TransferInstruction;

@Rule(key = "S6504")
public class ExecutableNotOwnedByRootCheck implements IacCheck {

  private static final String MESSAGE = "Make sure no write permissions are assigned to the executable.";
  private static final String MESSAGE_SECONDARY_OTHER_EXEC = "Other executable file.";
  private static final String MESSAGE_SECONDARY_CHOWN = "Sensitive file owner.";
  private static final Set<String> SENSITIVE_FILE_EXTENSION = Set.of("exe", "py", "rb", "pl", "lua", "js", "lisp", "sh", "jar", "war",
    "run", "bin", "bat", "ps1");

  private static final Set<String> COMPLIANT_CHOWN_VALUES = Set.of("root", "0", "");

  @Override
  public void initialize(InitContext init) {
    init.register(TransferInstruction.class, ExecutableNotOwnedByRootCheck::checkTransferInstruction);
  }

  private static void checkTransferInstruction(CheckContext ctx, TransferInstruction transferInstruction) {
    Flag sensitiveChownFlag = getSensitiveChownFlag(transferInstruction);

    if (sensitiveChownFlag != null) {
      List<Argument> sensitiveFiles = getSensitiveFiles(transferInstruction.srcs());

      if (isNonRootUser(sensitiveChownFlag)) {
        reportIssue(ctx, sensitiveChownFlag, sensitiveFiles);
      }

      Chmod chmod = getChmod(transferInstruction);
      if (chmod == null) {
        if (!sensitiveFiles.isEmpty()) {
          reportIssue(ctx, sensitiveChownFlag, sensitiveFiles);
        }
      } else if (isSensitiveChmod(sensitiveChownFlag, sensitiveFiles, chmod)) {
        reportIssue(ctx, sensitiveChownFlag, sensitiveFiles);
      }
    }
  }

  private static boolean isSensitiveChmod(Flag sensitiveChownFlag, List<Argument> sensitiveFiles, Chmod chmod) {
    return !isRootUserAndGroupHasNoWritePermission(sensitiveChownFlag, chmod)
      && isSensitiveWriteChmod(chmod)
      && (isSensitiveExecuteChmod(chmod) || !sensitiveFiles.isEmpty());
  }

  private static void reportIssue(CheckContext ctx, Flag sensitiveChownFlag, List<Argument> sensitiveFiles) {
    if (sensitiveFiles.isEmpty()) {
      ctx.reportIssue(sensitiveChownFlag, MESSAGE);
    } else {
      HasTextRange primaryLocation = sensitiveFiles.get(0);
      List<SecondaryLocation> secondaryLocations = new ArrayList<>();
      for (Argument otherExecutable : sensitiveFiles.subList(1, sensitiveFiles.size())) {
        secondaryLocations.add(new SecondaryLocation(otherExecutable, MESSAGE_SECONDARY_OTHER_EXEC));
      }
      secondaryLocations.add(new SecondaryLocation(sensitiveChownFlag, MESSAGE_SECONDARY_CHOWN));
      ctx.reportIssue(primaryLocation, MESSAGE, secondaryLocations);
    }
  }

  @CheckForNull
  private static Flag getSensitiveChownFlag(TransferInstruction transferInstruction) {
    return transferInstruction.options().stream()
      .filter(f -> f.name().equals("chown"))
      .filter(ExecutableNotOwnedByRootCheck::isSensitiveUser)
      .findFirst()
      .orElse(null);
  }

  private static boolean isSensitiveUser(Flag chownFlag) {
    ArgumentResolution resolvedArgArgument = ArgumentResolution.of(chownFlag.value());
    return resolvedArgArgument.isResolved() && isNonRootChown(resolvedArgArgument.value());
  }

  @CheckForNull
  private static Chmod getChmod(TransferInstruction transferInstruction) {
    return transferInstruction.options().stream()
      .filter(f -> f.name().equals("chmod"))
      .map(f -> ArgumentResolution.of(f.value()))
      .filter(ArgumentResolution::isResolved)
      .map(argResolved -> new Chmod(null, null, argResolved.value()))
      .findFirst()
      .orElse(null);
  }

  private static List<Argument> getSensitiveFiles(List<Argument> arguments) {
    return arguments.stream()
      .map(ArgumentResolution::of)
      .filter(ExecutableNotOwnedByRootCheck::isSensitiveFile)
      .map(ArgumentResolution::argument)
      .collect(Collectors.toList());
  }

  private static boolean isSensitiveFile(ArgumentResolution argumentResolution) {
    return argumentResolution.isResolved() && SENSITIVE_FILE_EXTENSION.contains(CheckUtils.getFileExtension(argumentResolution.value()));
  }

  private static boolean isSensitiveWriteChmod(Chmod chmod) {
    return chmod.hasPermission("u+w") || chmod.hasPermission("g+w");
  }

  private static boolean isSensitiveExecuteChmod(Chmod chmod) {
    return chmod.hasPermission("u+x") || chmod.hasPermission("g+x") || chmod.hasPermission("o+x");
  }

  // true if user value is any of ['root', '0', ''], group value is not from this list, and group has write permissions
  // for example --chown=root:bar --chmod=664
  private static boolean isRootUserAndGroupHasNoWritePermission(Flag chown, Chmod chmod) {
    ArgumentResolution resolvedChown = ArgumentResolution.of(chown.value());
    boolean isRootUser = !isNonRootAtId(resolvedChown.value(), 0);
    return !chmod.hasPermission("g+w") && isRootUser && isNonRootAtId(resolvedChown.value(), 1);
  }

  // true if the user value is different from ['root', '0', ''], for example 'foo:root'
  private static boolean isNonRootUser(Flag sensitiveChownFlag) {
    ArgumentResolution resolvedChown = ArgumentResolution.of(sensitiveChownFlag.value());
    return isNonRootAtId(resolvedChown.value(), 0);
  }

  // true if any of the user or group value is different from ['root', '0', ''], for example 'root:foo'
  private static boolean isNonRootChown(String chownValue) {
    return isNonRootAtId(chownValue, 0) || isNonRootAtId(chownValue, 1);
  }

  // true if the value at the specified id (0 for user, 1 for group) is not from ['root', '0', '']
  static boolean isNonRootAtId(String chownValue, int indexToCheck) {
    String[] split = chownValue.split(":");
    if (split.length > indexToCheck) {
      return !COMPLIANT_CHOWN_VALUES.contains(split[indexToCheck]);
    }
    return false;
  }
}
