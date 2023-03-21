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
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S4423")
public class WeakSslTlsProtocolsCheck implements IacCheck {

  private static final String MESSAGE = "Change this code to enforce TLS 1.2 or above.";

  private static final CommandDetector WEAK_CURL_TLS_MAX = CommandDetector.builder()
    .with("curl")
    .withOptionalRepeatingExcept("--tls-max")
    .with("--tls-max")
    .with(Set.of("1.0", "1.1"))
    .build();

  public static final Set<String> INSECURE_FLAGS = Set.of(
    "--sslv2",
    "-2",
    "--sslv3",
    "-3",
    "--tlsv1.0",
    "--tlsv1",
    "-1",
    "--tlsv1.1");

  private static final CommandDetector WEAK_CURL_PROTOCOLS = CommandDetector.builder()
    .with("curl")
    .withOptionalRepeatingExcept(INSECURE_FLAGS)
    .with(INSECURE_FLAGS)
    .build();

  private static final List<CommandDetector> COMMANDS = List.of(WEAK_CURL_TLS_MAX, WEAK_CURL_PROTOCOLS);

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, WeakSslTlsProtocolsCheck::checkRun);
  }

  private static void checkRun(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);
    COMMANDS.forEach(detector -> detector.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE)));
  }
}
