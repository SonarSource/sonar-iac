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
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

import static org.sonar.iac.docker.checks.utils.StringPredicate.containsIgnoreQuotes;
import static org.sonar.iac.docker.checks.utils.StringPredicate.equalsIgnoreQuotes;

@Rule(key = "S4423")
public class WeakSslTlsProtocolsCheck implements IacCheck {

  private static final String MESSAGE = "Change this code to enforce TLS 1.2 or above.";

  public static final Set<String> WEAK_TLS_MAX_VERSIONS = Set.of("1.0", "1.1");
  private static final CommandDetector WEAK_CURL_TLS_MAX = CommandDetector.builder()
    .with("curl")
    .withOptionalRepeatingExcept("--tls-max")
    .with("--tls-max")
    .with(containsIgnoreQuotes(WEAK_TLS_MAX_VERSIONS))
    .build();

  public static final Set<String> INSECURE_CURL_FLAGS = Set.of(
    "--sslv2",
    "-2",
    "--sslv3",
    "-3",
    "--tlsv1.0",
    "--tlsv1",
    "-1",
    "--tlsv1.1");

  private static final Predicate<String> INSECURE_CURL_PREDICATE = containsIgnoreQuotes(INSECURE_CURL_FLAGS);
  private static final CommandDetector WEAK_CURL_PROTOCOLS = CommandDetector.builder()
    .with("curl")
    .withOptionalRepeatingExcept(INSECURE_CURL_PREDICATE)
    .with(INSECURE_CURL_PREDICATE)
    .build();

  public static final String WGET_SECURE_PROTOCOL_FLAG = "--secure-protocol";
  public static final Set<String> INSECURE_WGET_PROTOCOLS = Set.of("SSLv2", "SSLv3", "TLSv1", "TLSv1_1");
  private static final Predicate<String> WGET_SECURE_PROTOCOL_PREDICATE = equalsIgnoreQuotes(WGET_SECURE_PROTOCOL_FLAG);
  private static final CommandDetector WEAK_WGET_PROTOCOLS = CommandDetector.builder()
    .with("wget")
    .withOptionalRepeatingExcept(WGET_SECURE_PROTOCOL_PREDICATE)
    .with(WGET_SECURE_PROTOCOL_PREDICATE)
    .with(containsIgnoreQuotes(INSECURE_WGET_PROTOCOLS))
    .build();

  private static final Pattern WEAK_WGET_PROTOCOLS_EQUAL_REGEX = Pattern.compile("([\"'])?--secure-protocol=([\"'])?((SSLv2)|(SSLv3)|(TLSv1)|(TLSv1_1))([\"'])?");

  private static final CommandDetector WEAK_WGET_PROTOCOLS_EQUAL_SYNTAX = CommandDetector.builder()
    .with("wget")
    .withOptionalRepeatingExcept(s -> WEAK_WGET_PROTOCOLS_EQUAL_REGEX.matcher(s).matches())
    .with(s -> WEAK_WGET_PROTOCOLS_EQUAL_REGEX.matcher(s).matches())
    .build();

  private static final List<CommandDetector> COMMANDS = List.of(
    WEAK_CURL_TLS_MAX,
    WEAK_CURL_PROTOCOLS,
    WEAK_WGET_PROTOCOLS,
    WEAK_WGET_PROTOCOLS_EQUAL_SYNTAX);

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, WeakSslTlsProtocolsCheck::checkRun);
  }

  private static void checkRun(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);
    COMMANDS.forEach(detector -> detector.searchWithSplit(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE)));
  }
}
