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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

import static java.util.function.Predicate.not;
import static org.sonar.iac.docker.checks.utils.StringPredicate.equalsIgnoreQuotes;
import static org.sonar.iac.docker.checks.utils.StringPredicate.startsWithIgnoreQuotes;

@Rule(key = "S6506")
public class ClearTextProtocolDowngradeCheck implements IacCheck {

  private static final String CURL_MESSAGE = "Not enforcing HTTPS here might allow for redirections to insecure websites. Make sure it is safe here.";
  private static final String WGET_MESSAGE = "Not disabling redirects might allow for redirections to insecure websites. Make sure it is safe here.";
  private static final String CURL_COMMAND = "curl";
  private static final String PROTO_FLAG = "--proto";
  private static final Set<String> WGET_NO_REDIRECT_FLAGS = Set.of("--max-redirect=0", "--max-redirect='0'", "--max-redirect=\"0\"");
  private static final Set<String> REDIRECTION_FLAGS = Set.of("-L", "--location");
  private static final Set<String> SENSITIVE_FLAGS = Set.of("-L", "--location", PROTO_FLAG);
  // Curl supports "short options immediately next to each other", like -SfsL, we need special detection for this.
  private static final String REDIRECTION_SHORT_FLAG = "L";
  private static final Predicate<String> EQUALS_PROTO_FLAG_OPTION = equalsIgnoreQuotes("=https");
  private static final Predicate<String> SENSITIVE_HTTPS_URL_BEGINNING = startsWithIgnoreQuotes("https");

  private static final Predicate<String> SHORT_FLAG = s -> s.startsWith("-") && (s.length() == 1 || s.charAt(1) != '-');
  private static final Predicate<String> LONG_FLAG = s -> s.startsWith("--");
  private static final Predicate<String> LONG_OTHER_FLAG = LONG_FLAG.and(not(longFlagContainingOneOf(SENSITIVE_FLAGS)));
  private static final Predicate<String> SHORT_OTHER_FLAG = SHORT_FLAG.and(not(shortFlagContaining(REDIRECTION_SHORT_FLAG)));
  private static final Predicate<String> OPTIONAL_OTHER_FLAGS = LONG_OTHER_FLAG.or(SHORT_OTHER_FLAG);

  // common predicates of detectors
  private static final CommandDetector.Builder REDIRECTION_PREDICATES = CommandDetector.builder()
    .withOptionalRepeating(OPTIONAL_OTHER_FLAGS)
    .with(longFlagContainingOneOf(REDIRECTION_FLAGS).or(shortFlagContaining(REDIRECTION_SHORT_FLAG)))
    .withOptionalRepeating(OPTIONAL_OTHER_FLAGS);

  private static final CommandDetector.Builder PROTO_FLAG_MISSING_OPTION_PREDICATES = CommandDetector.builder()
    .withOptionalRepeating(OPTIONAL_OTHER_FLAGS)
    .with(PROTO_FLAG)
    .notWith(EQUALS_PROTO_FLAG_OPTION)
    .withOptionalRepeating(OPTIONAL_OTHER_FLAGS);

  private static final CommandDetector.Builder PROTO_FLAG_MISSING_PREDICATES = CommandDetector.builder()
    .withOptionalRepeating(OPTIONAL_OTHER_FLAGS)
    .notWith(PROTO_FLAG::equals)
    .withOptionalRepeating(OPTIONAL_OTHER_FLAGS);

  private static final CommandDetector.Builder PROTO_FLAG_WITH_WRONG_OPTION_PREDICATES = CommandDetector.builder()
    .withOptionalRepeating(OPTIONAL_OTHER_FLAGS)
    .with(PROTO_FLAG)
    .with(not(EQUALS_PROTO_FLAG_OPTION))
    .withOptionalRepeating(OPTIONAL_OTHER_FLAGS);

  // actual detectors
  // matching "curl -L --proto https://redirecttoinsecure.example.com"
  private static final CommandDetector SENSITIVE_CURL_COMMAND_FLAG_WITH_MISSING_OPTION = CommandDetector.builder()
    .with(CURL_COMMAND)
    .withPredicatesFrom(REDIRECTION_PREDICATES)
    .withPredicatesFrom(PROTO_FLAG_MISSING_OPTION_PREDICATES)
    .with(SENSITIVE_HTTPS_URL_BEGINNING)
    .build();

  // matching "curl --proto -L https://redirecttoinsecure.example.com"
  private static final CommandDetector SENSITIVE_CURL_COMMAND_FLAG_WITH_MISSING_OPTION_DIFF_ORDER = CommandDetector.builder()
    .with(CURL_COMMAND)
    .withPredicatesFrom(PROTO_FLAG_MISSING_OPTION_PREDICATES)
    .withPredicatesFrom(REDIRECTION_PREDICATES)
    .with(SENSITIVE_HTTPS_URL_BEGINNING)
    .build();

  // matching "curl -L https://redirecttoinsecure.example.com"
  private static final CommandDetector SENSITIVE_CURL_COMMAND_MISSING_FLAG = CommandDetector.builder()
    .with(CURL_COMMAND)
    .withPredicatesFrom(REDIRECTION_PREDICATES)
    .withPredicatesFrom(PROTO_FLAG_MISSING_PREDICATES)
    .with(SENSITIVE_HTTPS_URL_BEGINNING)
    .build();

  // matching "curl -L --proto =foo https://redirecttoinsecure.example.com"
  private static final CommandDetector SENSITIVE_CURL_COMMAND_FLAG_WITH_WRONG_OPTION = CommandDetector.builder()
    .with(CURL_COMMAND)
    .withPredicatesFrom(REDIRECTION_PREDICATES)
    .withPredicatesFrom(PROTO_FLAG_WITH_WRONG_OPTION_PREDICATES)
    .with(SENSITIVE_HTTPS_URL_BEGINNING)
    .build();

  // matching "curl --proto =foo -L https://redirecttoinsecure.example.com"
  private static final CommandDetector SENSITIVE_CURL_COMMAND_FLAG_WITH_WRONG_OPTION_DIFF_ORDER = CommandDetector.builder()
    .with(CURL_COMMAND)
    .withPredicatesFrom(PROTO_FLAG_WITH_WRONG_OPTION_PREDICATES)
    .withPredicatesFrom(REDIRECTION_PREDICATES)
    .with(SENSITIVE_HTTPS_URL_BEGINNING)
    .build();

  // For wget, arguments can be both before and after the URL, unless there is `--` before the URL.
  private static final CommandDetector WGET_DETECTOR = CommandDetector.builder()
    .with("wget")
    .withAnyFlagExcept(WGET_NO_REDIRECT_FLAGS)
    .with(SENSITIVE_HTTPS_URL_BEGINNING)
    .withAnyFlagExcept(WGET_NO_REDIRECT_FLAGS)
    .build();

  private static final Set<CommandDetector> SENSITIVE_CURL_COMMAND_DETECTORS = Set.of(
    SENSITIVE_CURL_COMMAND_FLAG_WITH_MISSING_OPTION,
    SENSITIVE_CURL_COMMAND_FLAG_WITH_MISSING_OPTION_DIFF_ORDER,
    SENSITIVE_CURL_COMMAND_MISSING_FLAG,
    SENSITIVE_CURL_COMMAND_FLAG_WITH_WRONG_OPTION,
    SENSITIVE_CURL_COMMAND_FLAG_WITH_WRONG_OPTION_DIFF_ORDER);

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, ClearTextProtocolDowngradeCheck::checkRunInstruction);
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);

    SENSITIVE_CURL_COMMAND_DETECTORS.forEach(
      commandDetector -> commandDetector.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, CURL_MESSAGE)));
    WGET_DETECTOR.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, WGET_MESSAGE));
  }

  private static Predicate<String> longFlagContainingOneOf(Collection<String> flags) {
    return LONG_FLAG.and(flags::contains);
  }

  private static Predicate<String> shortFlagContaining(String flag) {
    return SHORT_FLAG.and(s -> s.contains(flag));
  }
}
