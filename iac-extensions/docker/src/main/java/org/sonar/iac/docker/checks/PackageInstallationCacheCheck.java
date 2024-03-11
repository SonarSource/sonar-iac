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
package org.sonar.iac.docker.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.docker.checks.utils.ArgumentResolutionSplitter;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

@Rule(key = "S6587")
public class PackageInstallationCacheCheck implements IacCheck {

  private static final String MESSAGE = "Remove cache after installing packages.";

  private static final Set<String> APK_CACHE_LOCATIONS = Set.of("/etc/apk/cache/*", "/var/cache/apk/*");
  private static final Set<String> APT_COMMANDS = Set.of("apt", "apt-get", "aptitude");
  private static final Set<String> APT_CACHE_LOCATIONS = Set.of("/var/lib/apt/lists/*");
  private static final Predicate<String> containsROrF = s -> s.toLowerCase(Locale.ROOT).contains("r") || s.contains("f");
  private static final Predicate<String> isFlag = s -> s.startsWith("-");
  private static final Predicate<String> flagWithNecessaryRmOptions = isFlag.and(containsROrF);
  private static final Predicate<String> flagWithoutContainingNecessaryFlags = isFlag.and(not(flagWithNecessaryRmOptions));

  private static final CommandDetector APK_ADD = CommandDetector.builder()
    .with("apk")
    .withAnyFlagExcept("--no-cache")
    .with("add")
    .withAnyFlagExcept("--no-cache")
    .build();

  private static final CommandDetector APK_CLEAN = buildCleanCacheDetector(Set.of("apk"));
  private static final CommandDetector REMOVE_APK_CACHE_DETECTOR = buildRemoveCacheDetector(APK_CACHE_LOCATIONS);
  private static final CommandDetector APT_INSTALL = CommandDetector.builder()
    .with(APT_COMMANDS)
    .withAnyFlag()
    .with("install")
    .withAnyFlag()
    .build();

  private static final CommandDetector APT_CLEAN = buildCleanCacheDetector(APT_COMMANDS);
  private static final CommandDetector REMOVE_APT_CACHE_DETECTOR = buildRemoveCacheDetector(APT_CACHE_LOCATIONS);

  private static CommandDetector buildCleanCacheDetector(Set<String> commandNames) {
    var builder = CommandDetector.builder()
      .with(commandNames);

    if (commandNames.contains("apk")) {
      builder.with("cache");
    }

    return builder
      .with("clean")
      .build();
  }

  private static CommandDetector buildRemoveCacheDetector(Set<String> cacheLocations) {
    return CommandDetector.builder()
      .with("rm")
      .withOptionalRepeating(flagWithoutContainingNecessaryFlags)
      .with(flagWithNecessaryRmOptions)
      .withAnyFlag()
      .with(cacheLocations)
      .build();
  }

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, PackageInstallationCacheCheck::checkRunInstruction);
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);

    SeparatedList<List<ArgumentResolution>, String> splitCommands = ArgumentResolutionSplitter.splitCommands(resolvedArgument);

    List<CommandDetector.Command> sensitiveApkInstallCommands = new ArrayList<>();
    List<CommandDetector.Command> sensitiveAptInstallCommands = new ArrayList<>();

    for (List<ArgumentResolution> commands : splitCommands.elements()) {
      analyzeCommands(commands, sensitiveApkInstallCommands, APK_ADD, REMOVE_APK_CACHE_DETECTOR, APK_CLEAN);
      analyzeCommands(commands, sensitiveAptInstallCommands, APT_INSTALL, REMOVE_APT_CACHE_DETECTOR, APT_CLEAN);
    }

    sensitiveApkInstallCommands.forEach(command -> ctx.reportIssue(command, MESSAGE));
    sensitiveAptInstallCommands.forEach(command -> ctx.reportIssue(command, MESSAGE));
  }

  private static void analyzeCommands(
    List<ArgumentResolution> commandsToSearchIn,
    List<CommandDetector.Command> sensitiveInstallCommands,
    CommandDetector installDetector,
    CommandDetector removeCacheCommandDetector,
    CommandDetector cleanCacheCommandDetector) {

    sensitiveInstallCommands.addAll(installDetector.searchWithoutSplit(commandsToSearchIn));

    if (!sensitiveInstallCommands.isEmpty()) {
      List<CommandDetector.Command> cacheCleaningCommands = detectRemoveCacheCommands(commandsToSearchIn, removeCacheCommandDetector);
      removeCacheCleanedInstallCommands(sensitiveInstallCommands, cacheCleaningCommands);
    }

    if (!sensitiveInstallCommands.isEmpty()) {
      List<CommandDetector.Command> cacheCleaningCommands = cleanCacheCommandDetector.searchWithoutSplit(commandsToSearchIn);
      removeCacheCleanedInstallCommands(sensitiveInstallCommands, cacheCleaningCommands);
    }
  }

  private static List<CommandDetector.Command> detectRemoveCacheCommands(
    List<ArgumentResolution> commandsToSearchIn, CommandDetector removeCacheDetector) {

    return removeCacheDetector
      .searchWithoutSplit(commandsToSearchIn).stream()
      .filter(PackageInstallationCacheCheck::verifyActualCacheRemovalCommand)
      .toList();
  }

  private static void removeCacheCleanedInstallCommands(List<CommandDetector.Command> installCommands, List<CommandDetector.Command> cacheCleaningCommands) {
    if (cacheCleaningCommands.isEmpty()) {
      return;
    }
    // only the last one is important
    var lastCacheCleanCommand = cacheCleaningCommands.get(cacheCleaningCommands.size() - 1);
    int latestIndexOfRemovableInstall = -1;
    for (int j = installCommands.size() - 1; j >= 0; j--) {
      if (startsBefore(installCommands.get(j), lastCacheCleanCommand)) {
        latestIndexOfRemovableInstall = j;
        break;
      }
    }
    installCommands.subList(0, latestIndexOfRemovableInstall + 1).clear();
  }

  private static boolean startsBefore(HasTextRange first, HasTextRange second) {
    return first.textRange().start().compareTo(second.textRange().start()) < 0;
  }

  public static boolean verifyActualCacheRemovalCommand(CommandDetector.Command command) {
    var flagR = false;
    var flagF = false;

    List<ArgumentResolution> resolvedArguments = command.getResolvedArguments();

    // first element of list is always "rm", last element of list is always cache location
    for (var i = 1; i < resolvedArguments.size() - 1; i++) {
      String current = resolvedArguments.get(i).value();
      if (!flagR && containsFlagR(current)) {
        flagR = true;
      }
      if (!flagF && containsFlagF(current)) {
        flagF = true;
      }
    }

    return flagF && flagR;
  }

  private static boolean containsFlagF(String content) {
    return "--force".equals(content) || (!content.startsWith("--") && content.contains("f"));
  }

  private static boolean containsFlagR(String content) {
    return "--recursive".equals(content) || (!content.startsWith("--") && content.toLowerCase(Locale.ROOT).contains("r"));
  }
}
