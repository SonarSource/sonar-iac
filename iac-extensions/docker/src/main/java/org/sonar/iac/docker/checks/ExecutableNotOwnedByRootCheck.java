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
import org.apache.commons.io.FilenameUtils;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
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

  @Override
  public void initialize(InitContext init) {
    init.register(TransferInstruction.class, ExecutableNotOwnedByRootCheck::checkTransferInstruction);
  }

  private static void checkTransferInstruction(CheckContext ctx, TransferInstruction transferInstruction) {
    Flag sensitiveChownFlag = getSensitiveChownFlag(transferInstruction);
    if (sensitiveChownFlag != null) {
      List<Argument> sensitiveFiles = getSensitiveFiles(transferInstruction.srcs());
      Chmod chmod = getChmod(transferInstruction);

      if (chmod == null) {
        if (!sensitiveFiles.isEmpty()) {
          reportIssue(ctx, sensitiveChownFlag, sensitiveFiles);
        }
      } else if (isChmodWriteSensitive(chmod) && (isChmodExecuteSensitive(chmod) || !sensitiveFiles.isEmpty())) {
        reportIssue(ctx, sensitiveChownFlag, sensitiveFiles);
      }
    }
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
      .findFirst().orElse(null);
  }

  private static boolean isSensitiveUser(Flag chownFlag) {
    ArgumentResolution resolvedArgArgument = ArgumentResolution.of(chownFlag.value());
    return resolvedArgArgument.is(ArgumentResolution.Status.RESOLVED) && !"root".equals(resolvedArgArgument.value());
  }

  @CheckForNull
  private static Chmod getChmod(TransferInstruction transferInstruction) {
    return transferInstruction.options().stream()
      .filter(f -> f.name().equals("chmod"))
      .map(f -> ArgumentResolution.of(f.value()))
      .filter(argResolved -> argResolved.is(ArgumentResolution.Status.RESOLVED))
      .map(argResolved -> new Chmod(argResolved.value()))
      .findFirst().orElse(null);
  }

  private static List<Argument> getSensitiveFiles(List<Argument> arguments) {
    return arguments.stream()
      .map(ArgumentResolution::of)
      .filter(ExecutableNotOwnedByRootCheck::isFileSensitive)
      .map(ArgumentResolution::argument)
      .collect(Collectors.toList());
  }

  private static boolean isFileSensitive(ArgumentResolution argumentResolution) {
    return argumentResolution.is(ArgumentResolution.Status.RESOLVED)
      && SENSITIVE_FILE_EXTENSION.contains(FilenameUtils.getExtension(argumentResolution.value()));
  }

  private static boolean isChmodWriteSensitive(Chmod chmod) {
    return chmod.hasPermission("u+w") || chmod.hasPermission("g+w");
  }

  private static boolean isChmodExecuteSensitive(Chmod chmod) {
    return chmod.hasPermission("u+x") || chmod.hasPermission("g+x") || chmod.hasPermission("o+x");
  }
}
