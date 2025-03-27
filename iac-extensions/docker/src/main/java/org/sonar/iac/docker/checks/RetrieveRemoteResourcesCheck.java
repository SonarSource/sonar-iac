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

import java.util.List;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.checks.utils.ArgumentResolutionSplitter;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.Variable;

import static org.sonar.iac.docker.checks.utils.command.StandardCommandDetectors.shortFlagPredicate;
import static org.sonar.iac.docker.checks.utils.command.StringPredicate.startsWithIgnoreQuotes;

@Rule(key = "S7026")
public class RetrieveRemoteResourcesCheck implements IacCheck {

  private static final String MESSAGE = "Replace this invocation of \"%s\" with the ADD instruction.";
  private static final String WGET = "wget";
  private static final String CURL = "curl";

  private static final List<String> WGET_FORBIDDEN_FLAGS = List.of("--http-user", "--http-password", "--proxy-user", "--proxy-password",
    "--load-cookies", "--header", "--method", "--body-data", "--referer", "--save-headers", "--user-agent", "-U", "--post-data", "--post-file");
  private static final Predicate<String> WGET_DOWNLOAD_FLAG_PREDICATE = startsWithIgnoreQuotes("-O", "--output-document");

  private static final List<String> CURL_FORBIDDEN_FLAGS = List.of("--anyauth", "--basic", "--digest", "--ntlm", "--negotiate",
    "--proxy-anyauth", "--proxy-basic", "--proxy-digest", "--proxy-ntlm", "--proxy-negotiate", "--user", "-u", "--oauth2-bearer",
    "--proxy-user", "-U", "--tlsuser", "--proxy-tlspassword", "--tlspassword", "--proxy-tlspassword", "--proxy-tlsuser", "--tlsuser", "-b",
    "--cookie", "-c", "--cookie-jar", "--data", "-d", "--data-raw", "--data-ascii", "--data-binary", "--data-raw", "--data-urlencode",
    "--form", "-F", "--form-escape", "--form-string", "--header", "-H", "--json", "--referer", "-e", "--request", "-X", "--user-agent", "-A");
  private static final List<String> CURL_STDOUT_REDIRECT = List.of(">", ">>", "1>", "1>>");
  private static final Predicate<String> CURL_DOWNLOAD_FLAG_PREDICATE = startsWithIgnoreQuotes("-o", "--output", "--remote-name");
  private static final Predicate<String> CURL_SHORT_DOWNLOAD_FLAG = shortFlagPredicate('O');

  private static final Predicate<String> URL_PREDICATE = startsWithIgnoreQuotes("http");

  // wget -O /path/to/resource https://example.com/resource
  // wget https://example.com/resource -O /path/to/resource
  private static final CommandDetector WGET_DOWNLOAD_DETECTOR = CommandDetector.builder()
    .with(WGET)
    .contains(URL_PREDICATE)
    .contains(WGET_DOWNLOAD_FLAG_PREDICATE)
    .build();

  // curl -o output.txt https://example.com/resource
  // curl -LOv https://example.com/resource
  // curl https://example.com/resource -o output.txt
  // curl https://example.com/resource -LOv
  private static final CommandDetector CURL_DOWNLOAD_DETECTOR = CommandDetector.builder()
    .with(CURL)
    .contains(CURL_DOWNLOAD_FLAG_PREDICATE.or(CURL_SHORT_DOWNLOAD_FLAG))
    .contains(URL_PREDICATE)
    .build();

  // curl https://example.com/resource > output.txt
  private static final CommandDetector CURL_REDIRECT_STDOUT_DETECTOR = CommandDetector.builder()
    .with(CURL)
    .withOptionalRepeating(URL_PREDICATE.negate())
    .with(URL_PREDICATE)
    .withOptionalRepeatingExcept(CURL_STDOUT_REDIRECT)
    .with(CURL_STDOUT_REDIRECT)
    .with(str -> !"/dev/null".equals(str))
    .build();

  private static final List<CommandDetector> CURL_DETECTORS = List.of(
    CURL_DOWNLOAD_DETECTOR,
    CURL_REDIRECT_STDOUT_DETECTOR);

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, RetrieveRemoteResourcesCheck::check);
  }

  private static void check(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);
    SeparatedList<List<ArgumentResolution>, String> splitCommands = ArgumentResolutionSplitter.splitCommands(resolvedArgument);
    // Check only the first and last commands, as they can be extracted into ADD instruction
    var commands = splitCommands.elements();
    checkArgumentsForWget(ctx, commands.get(0));
    checkArgumentsForCurl(ctx, commands.get(0));
    if (commands.size() > 1) {
      checkArgumentsForWget(ctx, commands.get(commands.size() - 1));
      checkArgumentsForCurl(ctx, commands.get(commands.size() - 1));
    }
  }

  private static void checkArgumentsForWget(CheckContext ctx, List<ArgumentResolution> args) {
    WGET_DOWNLOAD_DETECTOR.search(args).forEach((CommandDetector.Command command) -> {
      if (doesNotContainFlags(args, WGET_FORBIDDEN_FLAGS) && doesNotContainEnvVariables(args) && doesNotRedirectOutputToStdout("-O", "--output-document", args)) {
        reportIssue(ctx, args, WGET);
      }
    });
  }

  private static void checkArgumentsForCurl(CheckContext ctx, List<ArgumentResolution> args) {
    for (CommandDetector curlDetector : CURL_DETECTORS) {
      curlDetector.search(args).forEach((CommandDetector.Command command) -> {
        if (doesNotContainFlags(args, CURL_FORBIDDEN_FLAGS) && doesNotContainEnvVariables(args) && doesNotRedirectOutputToStdout("-o", "--output", args)) {
          reportIssue(ctx, args, CURL);
        }
      });
    }
  }

  /**
   * Check if the list of resolved arguments contain the short flag with dash concatenated (E.g {@code -o-}) or the short/long flag followed by a separated
   * dash (E.g {@code -o -} or {@code --output -}).
   * This would redirect the output to stdout according to <a href="https://curl.se/docs/manpage.html">documentation</>.
   */
  private static boolean doesNotRedirectOutputToStdout(String shortFlag, String longFlag, List<ArgumentResolution> args) {
    var isOutputOptionArgument = false;
    var condensedShortFlagStdout = shortFlag + "-";

    for (ArgumentResolution arg : args) {
      String value = arg.value();
      if (condensedShortFlagStdout.equals(value) || (isOutputOptionArgument && "-".equals(value))) {
        return false;
      }
      isOutputOptionArgument = shortFlag.equals(value) || longFlag.equals(value);
    }
    return true;
  }

  private static boolean doesNotContainFlags(List<ArgumentResolution> args, List<String> flags) {
    return args.stream().noneMatch(
      arg -> flags.stream().anyMatch(flag -> arg.value().startsWith(flag)));
  }

  /**
   * Check if the command does not contain unresolved environment variables.
   * It can contain variables that come from Docker and not Bash, in that case the command can be extracted into an ADD instruction.
   */
  private static boolean doesNotContainEnvVariables(List<ArgumentResolution> args) {
    return args.stream().noneMatch(arg -> arg.status() == ArgumentResolution.Status.UNRESOLVED &&
      arg.argument().expressions().stream().anyMatch(RetrieveRemoteResourcesCheck::isVariable));
  }

  private static boolean isVariable(Tree tree) {
    if (tree instanceof Variable) {
      return true;
    }
    return tree.children().stream().anyMatch(RetrieveRemoteResourcesCheck::isVariable);
  }

  private static void reportIssue(CheckContext ctx, List<ArgumentResolution> args, String command) {
    int position = findPositionOf(args, command);
    var textRangeList = args.stream()
      .skip(position)
      .map(a -> a.argument().textRange())
      .toList();
    var textRange = TextRanges.merge(textRangeList);
    ctx.reportIssue(textRange, MESSAGE.formatted(command));
  }

  // visibility for tests
  static int findPositionOf(List<ArgumentResolution> args, String text) {
    for (var i = 0; i < args.size(); i++) {
      if (text.equals(args.get(i).value())) {
        return i;
      }
    }
    return 0;
  }
}
