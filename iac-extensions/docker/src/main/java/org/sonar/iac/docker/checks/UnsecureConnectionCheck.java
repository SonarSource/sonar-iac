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
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S4830")
public class UnsecureConnectionCheck implements IacCheck {

  private static final String MESSAGE = "Disabling TLS certificate verification is security-sensitive.";

  private static final Set<String> SENSITIVE_CURL_OPTION = Set.of("-k", "--insecure", "--proxy-insecure", "--doh-insecure");
  private static final CommandDetector SENSITIVE_CURL_COMMAND = CommandDetector.builder()
    .with("curl"::equals)
    .withOptionalRepeating(s -> !SENSITIVE_CURL_OPTION.contains(s) && s.startsWith("-"))
    .with(SENSITIVE_CURL_OPTION::contains)
    .build();
  private static final CommandDetector SENSITIVE_WGET_COMMAND = CommandDetector.builder()
    .with("wget"::equals)
    .withOptionalRepeating(s -> !"--no-check-certificate".equals(s) && s.startsWith("-"))
    .with("--no-check-certificate"::equals)
    .build();

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, UnsecureConnectionCheck::checkRun);
  }

  private static void checkRun(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = runInstruction.arguments().stream().map(ArgumentResolution::of).collect(Collectors.toList());

    SENSITIVE_CURL_COMMAND.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE));
    SENSITIVE_WGET_COMMAND.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE));
  }
}
