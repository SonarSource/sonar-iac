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

import static org.sonar.iac.docker.checks.utils.command.StandardCommandDetectors.shortFlagPredicate;
import static org.sonar.iac.docker.checks.utils.command.StringPredicate.startsWithIgnoreQuotes;

@Rule(key = "S7026")
public class RetrieveRemoteResourcesCheck implements IacCheck {

  private static final String MESSAGE = "Replace this invocation of %s with the ADD instruction.";
  private static final String WGET = "wget";
  private static final String CURL = "curl";

  private static final List<String> WGET_AUTH_FLAGS = List.of("--http-user", "--http-password", "--proxy-user", "--proxy-password", "--load-cookies");
  private static final List<String> WGET_REQUEST_FLAGS = List.of("--header", "--method", "--body-data", "--referer", "--save-headers",
    "--user-agent", "-U", "--post-data", "--post-file");
  private static final Predicate<String> WGET_DOWNLOAD_FLAG_PREDICATE = startsWithIgnoreQuotes("-O", "--output-document");

  private static final List<String> CURL_AUTH_FLAGS = List.of("--anyauth", "--basic", "--digest", "--ntlm", "--negotiate",
    "--proxy-anyauth", "--proxy-basic", "--proxy-digest", "--proxy-ntlm", "--proxy-negotiate", "--user", "-u", "--oauth2-bearer",
    "--proxy-user", "-U", "--tlsuser", "--proxy-tlspassword", "--tlspassword", "--proxy-tlspassword", "--proxy-tlsuser", "--tlsuser", "-b",
    "--cookie", "-c", "--cookie-jar");
  private static final List<String> CURL_REQUEST_FLAGS = List.of("--data", "-d", "--data-raw", "--data-ascii", "--data-binary",
    "--data-raw", "--data-urlencode", "--form", "-F", "--form-escape", "--form-string", "--header", "-H", "--json", "--referer", "-e",
    "--request", "-X", "--user-agent", "-A");
  private static final List<String> CURL_STDOUT_REDIRECT = List.of(">", ">>", "1>", "1>>");
  private static final Predicate<String> CURL_DOWNLOAD_FLAG_PREDICATE = startsWithIgnoreQuotes("-o", "--output", "-O", "--remote-name");
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
    splitCommands.elements().forEach((List<ArgumentResolution> args) -> {
      checkArgumentsForWget(ctx, args);
      checkArgumentsForCurl(ctx, args);
    });
  }

  private static void checkArgumentsForWget(CheckContext ctx, List<ArgumentResolution> args) {
    WGET_DOWNLOAD_DETECTOR.search(args).forEach((CommandDetector.Command command) -> {
      if (doesNotContainFlags(args, WGET_AUTH_FLAGS) && doesNotContainFlags(args, WGET_REQUEST_FLAGS)) {
        reportIssue(ctx, args, WGET);
      }
    });
  }

  private static void checkArgumentsForCurl(CheckContext ctx, List<ArgumentResolution> args) {
    for (CommandDetector curlDetector : CURL_DETECTORS) {
      curlDetector.search(args).forEach((CommandDetector.Command command) -> {
        if (doesNotContainFlags(args, CURL_AUTH_FLAGS) && doesNotContainFlags(args, CURL_REQUEST_FLAGS)) {
          reportIssue(ctx, args, CURL);
        }
      });
    }
  }

  private static boolean doesNotContainFlags(List<ArgumentResolution> args, List<String> flags) {
    return args.stream().noneMatch(
      arg -> flags.stream().anyMatch(flag -> arg.value().startsWith(flag)));
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
