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
import org.sonar.iac.docker.checks.utils.ShortFlagPredicate;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

import static org.sonar.iac.docker.checks.utils.StringPredicate.startsWithIgnoreQuotes;

@Rule(key = "S7026")
public class RetrieveRemoteResourcesCheck implements IacCheck {

  private static final String MESSAGE = "Replace this invocation of %s with the ADD instruction.";
  private static final String WGET = "wget";
  private static final String CURL = "curl";

  private static final List<String> WGET_AUTH_FLAGS = List.of("--http-user", "--http-password", "--proxy-user", "--proxy-password", "--load-cookies");
  private static final Predicate<String> WGET_DOWNLOAD_FLAG_PREDICATE = startsWithIgnoreQuotes("-O", "--output-document");
  private static final Predicate<String> URL_PREDICATE = startsWithIgnoreQuotes("http");

  private static final List<String> CURL_AUTH_FLAGS = List.of("--anyauth", "--basic", "--digest", "--ntlm", "--negotiate",
    "--proxy-anyauth", "--proxy-basic", "--proxy-digest", "--proxy-ntlm", "--proxy-negotiate", "--user", "-u", "--oauth2-bearer",
    "--proxy-user", "-U", "--tlsuser", "--proxy-tlspassword", "--tlspassword", "--proxy-tlspassword", "--proxy-tlsuser", "--tlsuser", "-b",
    "--cookie", "-c", "--cookie-jar");
  private static final List<String> CURL_STDOUT_REDIRECT = List.of(">", ">>", "1>", "1>>");
  private static final Predicate<String> CURL_DOWNLOAD_FLAG_PREDICATE = startsWithIgnoreQuotes("-o", "--output", "-O", "--remote-name");
  private static final Predicate<String> CURL_SHORT_DOWNLOAD_FLAG = new ShortFlagPredicate('O');

  // wget -O /path/to/resource https://example.com/resource
  private static final CommandDetector WGET_DOWNLOAD_FLAG_FIRST_DETECTOR = CommandDetector.builder()
    .with(WGET)
    .withOptionalRepeating(WGET_DOWNLOAD_FLAG_PREDICATE.negate())
    .with(WGET_DOWNLOAD_FLAG_PREDICATE)
    .withOptionalRepeating(URL_PREDICATE.negate())
    .with(URL_PREDICATE)
    .build();

  // wget https://example.com/resource -O /path/to/resource
  private static final CommandDetector WGET_URL_FIRST_DETECTOR = CommandDetector.builder()
    .with(WGET)
    .withOptionalRepeating(URL_PREDICATE.negate())
    .with(URL_PREDICATE)
    .withOptionalRepeating(WGET_DOWNLOAD_FLAG_PREDICATE.negate())
    .with(WGET_DOWNLOAD_FLAG_PREDICATE)
    .build();

  private static final CommandDetector WGET_AUTH_HEADERS_EQUALS = CommandDetector.builder()
    .with(startsWithIgnoreQuotes("--header=\"Authorization", "--header=\"X-Auth-Token"))
    .build();

  private static final CommandDetector WGET_AUTH_HEADERS_SPACE = CommandDetector.builder()
    .with("--header")
    .with(startsWithIgnoreQuotes("Authorization", "X-Auth-Token"))
    .build();

  private static final List<CommandDetector> WGET_DETECTORS = List.of(WGET_DOWNLOAD_FLAG_FIRST_DETECTOR, WGET_URL_FIRST_DETECTOR);

  // curl -o output.txt https://example.com/resource
  private static final CommandDetector CURL_DOWNLOAD_FLAG_FIRST_DETECTOR = CommandDetector.builder()
    .with(CURL)
    .withOptionalRepeating(CURL_DOWNLOAD_FLAG_PREDICATE.negate())
    .with(CURL_DOWNLOAD_FLAG_PREDICATE)
    .withOptionalRepeating(URL_PREDICATE.negate())
    .with(URL_PREDICATE)
    .build();

  // curl https://example.com/resource -o output.txt
  private static final CommandDetector CURL_URL_FIRST_DETECTOR = CommandDetector.builder()
    .with(CURL)
    .withOptionalRepeating(URL_PREDICATE.negate())
    .with(URL_PREDICATE)
    .withOptionalRepeating(CURL_DOWNLOAD_FLAG_PREDICATE.negate())
    .with(CURL_DOWNLOAD_FLAG_PREDICATE)
    .build();

  // curl -LOv https://example.com/resource
  private static final CommandDetector CURL_DOWNLOAD_SHORT_FLAG_FIRST_DETECTOR = CommandDetector.builder()
    .with(CURL)
    .withOptionalRepeating(CURL_SHORT_DOWNLOAD_FLAG.negate())
    .with(CURL_SHORT_DOWNLOAD_FLAG)
    .withOptionalRepeating(URL_PREDICATE.negate())
    .with(URL_PREDICATE)
    .build();

  // curl https://example.com/resource -LOv
  private static final CommandDetector CURL_URL_FIRST_DOWNLOAD_SHORT_FLAG_DETECTOR = CommandDetector.builder()
    .with(CURL)
    .withOptionalRepeating(URL_PREDICATE.negate())
    .with(URL_PREDICATE)
    .withOptionalRepeating(CURL_SHORT_DOWNLOAD_FLAG.negate())
    .with(CURL_SHORT_DOWNLOAD_FLAG)
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

  // -H "Authorization: Bearer token"
  private static final CommandDetector CURL_AUTH_HEADERS = CommandDetector.builder()
    .with(List.of("-H", "--header"))
    .with(startsWithIgnoreQuotes("Authorization", "X-Auth-Token"))
    .build();

  private static final List<CommandDetector> CURL_DETECTORS = List.of(
    CURL_DOWNLOAD_FLAG_FIRST_DETECTOR,
    CURL_URL_FIRST_DETECTOR,
    CURL_DOWNLOAD_SHORT_FLAG_FIRST_DETECTOR,
    CURL_URL_FIRST_DOWNLOAD_SHORT_FLAG_DETECTOR,
    CURL_REDIRECT_STDOUT_DETECTOR);

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, RetrieveRemoteResourcesCheck::check);
  }

  private static void check(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);

    WGET_DETECTORS.forEach((CommandDetector detector) -> {
      SeparatedList<List<ArgumentResolution>, String> splitCommands = ArgumentResolutionSplitter.splitCommands(resolvedArgument);
      splitCommands.elements().forEach(args -> checkArgumentForWget(ctx, detector, args));
    });
    CURL_DETECTORS.forEach((CommandDetector detector) -> {
      SeparatedList<List<ArgumentResolution>, String> splitCommands = ArgumentResolutionSplitter.splitCommands(resolvedArgument);
      splitCommands.elements().forEach(args -> checkArgumentForCurl(ctx, detector, args));
    });
  }

  private static void checkArgumentForWget(CheckContext ctx, CommandDetector detector, List<ArgumentResolution> args) {
    detector.search(args).forEach((CommandDetector.Command command) -> {
      if (!containsWgetAuthenticationFlags(args)) {
        reportIssue(ctx, args, WGET);
      }
    });
  }

  private static boolean containsWgetAuthenticationFlags(List<ArgumentResolution> args) {
    var containsSimpleAuthFlag = args.stream().anyMatch(arg -> WGET_AUTH_FLAGS.stream().anyMatch(flag -> arg.value().startsWith(flag)));
    return containsSimpleAuthFlag || containsWgetAuthByHeader(args);
  }

  private static boolean containsWgetAuthByHeader(List<ArgumentResolution> args) {
    return !WGET_AUTH_HEADERS_EQUALS.search(args).isEmpty() ||
      !WGET_AUTH_HEADERS_SPACE.search(args).isEmpty();
  }

  private static void checkArgumentForCurl(CheckContext ctx, CommandDetector detector, List<ArgumentResolution> args) {
    detector.search(args).forEach((CommandDetector.Command command) -> {
      if (!containsCurlAuthenticationFlags(args)) {
        reportIssue(ctx, args, CURL);
      }
    });
  }

  private static boolean containsCurlAuthenticationFlags(List<ArgumentResolution> args) {
    var containsSimpleAuthFlag = args.stream().anyMatch(arg -> CURL_AUTH_FLAGS.stream().anyMatch(flag -> arg.value().startsWith(flag)));
    return containsSimpleAuthFlag || containsCurlAuthByHeader(args);
  }

  private static boolean containsCurlAuthByHeader(List<ArgumentResolution> args) {
    return !CURL_AUTH_HEADERS.search(args).isEmpty();
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

  private static int findPositionOf(List<ArgumentResolution> args, String text) {
    for (var i = 0; i < args.size(); i++) {
      if (text.equals(args.get(i).value())) {
        return i;
      }
    }
    return 0;
  }
}
