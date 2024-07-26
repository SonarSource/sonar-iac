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

import java.util.List;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.checks.utils.ArgumentResolutionSplitter;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

import static org.sonar.iac.docker.checks.utils.StringPredicate.startsWithIgnoreQuotes;

@Rule(key = "S7026")
public class RetrieveRemoteResourcesCheck implements IacCheck {

  private static final String MESSAGE = "Replace this invocation of %s with the ADD instruction.";

  private static final Predicate<String> WGET_DOWNLOAD_FLAG_PREDICATE = startsWithIgnoreQuotes("-O", "--output-document");
  private static final Predicate<String> URL_PREDICATE = startsWithIgnoreQuotes("http");

  // wget -O /path/to/resource https://example.com/resource
  private static final CommandDetector WGET_DOWNLOAD_FLAG_FIRST_DETECTOR = CommandDetector.builder()
    .with("wget")
    .withOptionalRepeating(WGET_DOWNLOAD_FLAG_PREDICATE.negate())
    .with(WGET_DOWNLOAD_FLAG_PREDICATE)
    .withOptionalRepeating(URL_PREDICATE.negate())
    .with(URL_PREDICATE)
    .build();

  // wget https://example.com/resource -O /path/to/resource
  private static final CommandDetector WGET_URL_FIRST_DETECTOR = CommandDetector.builder()
    .with("wget")
    .withOptionalRepeating(URL_PREDICATE.negate())
    .with(URL_PREDICATE)
    .withOptionalRepeating(WGET_DOWNLOAD_FLAG_PREDICATE.negate())
    .with(WGET_DOWNLOAD_FLAG_PREDICATE)
    .build();

  private static final List<CommandDetector> WGET_DETECTORS = List.of(WGET_DOWNLOAD_FLAG_FIRST_DETECTOR, WGET_URL_FIRST_DETECTOR);
  private static final List<String> WGET_AUTH_FLAGS = List.of("--http-user", "--http-password", "--proxy-user", "--proxy-password", "--load-cookies");

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, RetrieveRemoteResourcesCheck::check);
  }

  private static void check(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);

    WGET_DETECTORS.forEach(detector -> {
      SeparatedList<List<ArgumentResolution>, String> splitCommands = ArgumentResolutionSplitter.splitCommands(resolvedArgument);
      splitCommands.elements().forEach(args -> checkArgument(ctx, detector, args));
    });
  }

  private static void checkArgument(CheckContext ctx, CommandDetector detector, List<ArgumentResolution> args) {
    detector.search(args).forEach(command -> {
      if (!containsWgetAuthenticationFlags(args)) {
        var textRangeList = args.stream().map(a -> a.argument().textRange())
          .toList();
        var textRange = TextRanges.merge(textRangeList);
        ctx.reportIssue(textRange, MESSAGE.formatted("wget"));
      }
    });
  }

  private static boolean containsWgetAuthenticationFlags(List<ArgumentResolution> args) {
    return args.stream().anyMatch(arg -> WGET_AUTH_FLAGS.stream().anyMatch(flag -> arg.value().startsWith(flag)));
  }
}
