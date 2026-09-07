/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.docker.checks.utils.TransferChmod;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.TransferInstruction;

@Rule(key = "S6504")
public class ExecutableNotOwnedByRootCheck implements IacCheck {

  private static final String MESSAGE = "Make sure the copied resource cannot be modified by a non-root user.";
  private static final String MESSAGE_SECONDARY_OTHER_EXEC = "Other copied resource.";
  private static final String MESSAGE_SECONDARY_CHOWN = "Sensitive file owner.";

  private static final Set<String> COMPLIANT_CHOWN_VALUES = Set.of("root", "0", "");

  private static final Pattern SENSITIVE_PATH_PATTERN = Pattern.compile("^\\/(bin|boot|dev|etc|lib|lib32|lib64|proc|root|usr|sbin)(\\/(.+)?)?$");
  private static final Pattern RISKY_EXTENSION_PATTERN = Pattern.compile("\\.(sh|bash|zsh|fish|py|rb|pl|php|bin|elf|so|service|timer|socket)$");
  private static final Pattern OCTAL_CHMOD_PATTERN = Pattern.compile("[0-7]{1,4}");

  @Override
  public void initialize(InitContext init) {
    init.register(TransferInstruction.class, ExecutableNotOwnedByRootCheck::checkTransferInstruction);
  }

  private static void checkTransferInstruction(CheckContext ctx, TransferInstruction transferInstruction) {
    var sensitiveChownFlag = getSensitiveChownFlag(transferInstruction);
    if (sensitiveChownFlag == null) {
      return;
    }

    if (hasSensitivePath(transferInstruction) || hasSensitiveExtension(transferInstruction)) {
      reportIssue(ctx, sensitiveChownFlag, transferInstruction.srcs());
    }
  }

  private static boolean hasSensitivePath(TransferInstruction transferInstruction) {
    var resolved = ArgumentResolution.of(transferInstruction.dest());
    return resolved.isResolved() && SENSITIVE_PATH_PATTERN.matcher(resolved.value()).matches();
  }

  private static boolean hasSensitiveExtension(TransferInstruction transferInstruction) {
    return transferInstruction.srcs().stream()
      .map(ArgumentResolution::of)
      .filter(ArgumentResolution::isResolved)
      .anyMatch(resolved -> RISKY_EXTENSION_PATTERN.matcher(resolved.value()).find());
  }

  private static void reportIssue(CheckContext ctx, Flag sensitiveChownFlag, List<Argument> srcs) {
    if (srcs.isEmpty()) {
      ctx.reportIssue(sensitiveChownFlag, MESSAGE);
    } else {
      HasTextRange primaryLocation = srcs.get(0);
      List<SecondaryLocation> secondaryLocations = new ArrayList<>();
      for (Argument otherFile : srcs.subList(1, srcs.size())) {
        secondaryLocations.add(new SecondaryLocation(otherFile, MESSAGE_SECONDARY_OTHER_EXEC));
      }
      secondaryLocations.add(new SecondaryLocation(sensitiveChownFlag, MESSAGE_SECONDARY_CHOWN));
      ctx.reportIssue(primaryLocation, MESSAGE, secondaryLocations);
    }
  }

  @Nullable
  private static Flag getSensitiveChownFlag(TransferInstruction transferInstruction) {
    return transferInstruction.options().stream()
      .filter(f -> f.name().equals("chown"))
      .filter(f -> isSensitiveChown(f, transferInstruction))
      .findFirst()
      .orElse(null);
  }

  // true if a chown option lets a non-root user modify the transferred resource
  private static boolean isSensitiveChown(Flag chownFlag, TransferInstruction transferInstruction) {
    var resolvedChown = ArgumentResolution.of(chownFlag.value());
    if (!resolvedChown.isResolved()) {
      return false;
    }

    var chownValue = resolvedChown.value();
    return isNonRootAtId(chownValue, 0)
      || (isNonRootAtId(chownValue, 1) && !hasNonWritableOctalChmod(transferInstruction));
  }

  // true if every chmod option is a resolved octal mode without group or other write access
  private static boolean hasNonWritableOctalChmod(TransferInstruction transferInstruction) {
    var chmods = TransferChmod.extractChmods(transferInstruction);
    return !chmods.isEmpty() && chmods.stream().allMatch(ExecutableNotOwnedByRootCheck::isNonWritableOctalChmod);
  }

  // true if a chmod option resolves to an octal mode without group or other write permission
  private static boolean isNonWritableOctalChmod(TransferChmod chmod) {
    if (!chmod.resolution().isResolved() || !OCTAL_CHMOD_PATTERN.matcher(chmod.resolution().value()).matches()) {
      return false;
    }

    return !chmod.hasPermission("g+w") && !chmod.hasPermission("o+w");
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
