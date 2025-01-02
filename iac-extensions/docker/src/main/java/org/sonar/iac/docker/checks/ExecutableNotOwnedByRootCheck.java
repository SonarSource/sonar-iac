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
package org.sonar.iac.docker.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.docker.checks.utils.ArgumentChmod;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.TransferInstruction;

@Rule(key = "S6504")
public class ExecutableNotOwnedByRootCheck implements IacCheck {

  private static final String MESSAGE = "Make sure no write permissions are assigned to the copied resource.";
  private static final String MESSAGE_SECONDARY_OTHER_EXEC = "Other copied resource.";
  private static final String MESSAGE_SECONDARY_CHOWN = "Sensitive file owner.";

  private static final Set<String> COMPLIANT_CHOWN_VALUES = Set.of("root", "0", "");

  @Override
  public void initialize(InitContext init) {
    init.register(TransferInstruction.class, ExecutableNotOwnedByRootCheck::checkTransferInstruction);
  }

  private static void checkTransferInstruction(CheckContext ctx, TransferInstruction transferInstruction) {
    var sensitiveChownFlag = getSensitiveChownFlag(transferInstruction);

    if (sensitiveChownFlag != null) {
      var sensitiveFiles = transferInstruction.srcs();

      if (isNonRootUser(sensitiveChownFlag)) {
        reportIssue(ctx, sensitiveChownFlag, sensitiveFiles);
      }

      var chmod = getChmod(transferInstruction);
      if (chmod == null) {
        if (!sensitiveFiles.isEmpty()) {
          reportIssue(ctx, sensitiveChownFlag, sensitiveFiles);
        }
      } else if (isSensitiveChmod(sensitiveChownFlag, sensitiveFiles, chmod)) {
        reportIssue(ctx, sensitiveChownFlag, sensitiveFiles);
      }
    }
  }

  private static boolean isSensitiveChmod(Flag sensitiveChownFlag, List<Argument> sensitiveFiles, ArgumentChmod chmod) {
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
    var resolvedArgArgument = ArgumentResolution.of(chownFlag.value());
    return resolvedArgArgument.isResolved() && isNonRootChown(resolvedArgArgument.value());
  }

  @CheckForNull
  private static ArgumentChmod getChmod(TransferInstruction transferInstruction) {
    return transferInstruction.options().stream()
      .filter(f -> f.name().equals("chmod"))
      .map(f -> ArgumentResolution.of(f.value()))
      .filter(ArgumentResolution::isResolved)
      .map(argResolved -> new ArgumentChmod(null, null, argResolved.value()))
      .findFirst()
      .orElse(null);
  }

  private static boolean isSensitiveWriteChmod(ArgumentChmod chmod) {
    return chmod.hasPermission("u+w") || chmod.hasPermission("g+w");
  }

  private static boolean isSensitiveExecuteChmod(ArgumentChmod chmod) {
    return chmod.hasPermission("u+x") || chmod.hasPermission("g+x") || chmod.hasPermission("o+x");
  }

  // true if user value is any of ['root', '0', ''], group value is not from this list, and group has write permissions
  // for example --chown=root:bar --chmod=664
  private static boolean isRootUserAndGroupHasNoWritePermission(Flag chown, ArgumentChmod chmod) {
    var resolvedChown = ArgumentResolution.of(chown.value());
    var isRootUser = !isNonRootAtId(resolvedChown.value(), 0);
    return !chmod.hasPermission("g+w") && isRootUser && isNonRootAtId(resolvedChown.value(), 1);
  }

  // true if the user value is different from ['root', '0', ''], for example 'foo:root'
  private static boolean isNonRootUser(Flag sensitiveChownFlag) {
    var resolvedChown = ArgumentResolution.of(sensitiveChownFlag.value());
    return isNonRootAtId(resolvedChown.value(), 0);
  }

  // true if any of the user or group value is different from ['root', '0', ''], for example 'root:foo'
  private static boolean isNonRootChown(String chownValue) {
    return isNonRootAtId(chownValue, 0) || isNonRootAtId(chownValue, 1);
  }

  // true if the value at the specified id (0 for user, 1 for group) is not from ['root', '0', '']
  static boolean isNonRootAtId(String chownValue, int indexToCheck) {
    var split = chownValue.split(":");
    if (split.length > indexToCheck) {
      return !COMPLIANT_CHOWN_VALUES.contains(split[indexToCheck]);
    }
    return false;
  }
}
