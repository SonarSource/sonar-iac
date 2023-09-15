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
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6584")
public class PackageManagerConsentFlagCheck implements IacCheck {
  private static final String MESSAGE = "Add consent flag so that this command doesn't require user confirmation.";
  private static final String ASSUME_YES_FLAG = "--assume-yes";
  private static final Map<String, CommandOption> MANAGERS_TO_CONSENT_FLAGS = Map.of(
    "apt-get", new CommandOption("-y", List.of("--yes", ASSUME_YES_FLAG)),
    "apt", new CommandOption("-y", List.of("--yes", ASSUME_YES_FLAG)),
    "aptitude", new CommandOption("-y", List.of(ASSUME_YES_FLAG)));
  private static final CommandDetector DEBIAN_PACKAGE_MANAGER_DETECTOR = CommandDetector.builder()
    .with(Set.of("apt", "apt-get", "aptitude"))
    .withAnyFlag()
    // these commands don't expect user confirmation even in interactive mode
    .notWith(Set.of("update", "check", "changelog", "indextargets", "autoclean", "auto-clean")::contains)
    .withOptionalRepeating(s -> true)
    .build();

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, PackageManagerConsentFlagCheck::checkRunInstruction);
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> argumentResolutions = CheckUtils.resolveInstructionArguments(runInstruction);
    DEBIAN_PACKAGE_MANAGER_DETECTOR.search(argumentResolutions).forEach(c -> {
      if (isPackageManagerInvocationWithoutConsent(c)) {
        ctx.reportIssue(c.textRange(), MESSAGE);
      }
    });

  }

  private static boolean isPackageManagerInvocationWithoutConsent(CommandDetector.Command command) {
    String commandName = command.getResolvedArguments().get(0).value();

    return command.getResolvedArguments().stream()
      .noneMatch(argumentResolution -> MANAGERS_TO_CONSENT_FLAGS.get(commandName).matches(argumentResolution.value()));
  }

  static class CommandOption {
    final String shortVariant;
    final List<String> longVariants;

    CommandOption(String shortVariant, List<String> longVariants) {
      this.shortVariant = shortVariant;
      this.longVariants = longVariants;
    }

    boolean matches(String flag) {
      if (flag.startsWith("--")) {
        for (String longVariant : longVariants) {
          if (longVariant.equals(flag)) {
            return true;
          }
        }
        return false;
      }

      // this takes into account that short options can be glued together
      // this way we will e.g. detect `y` in `-tmy`
      return flag.startsWith("-") && flag.indexOf(shortVariant.charAt(1)) != -1;
    }
  }
}
