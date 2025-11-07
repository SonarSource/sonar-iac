/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S4830")
public class UnsecureConnectionCheck implements IacCheck {

  private static final String MESSAGE = "Enable server certificate validation on this SSL/TLS connection.";

  private static final Set<String> SENSITIVE_CURL_OPTION = Set.of("-k", "--insecure", "--proxy-insecure", "--doh-insecure");

  private static final CommandDetector SENSITIVE_CURL_COMMAND = CommandDetector.builder()
    .with("curl")
    .withAnyFlagFollowedBy(SENSITIVE_CURL_OPTION)
    .build();

  private static final CommandDetector SENSITIVE_WGET_COMMAND = CommandDetector.builder()
    .with("wget")
    .withAnyFlagFollowedBy("--no-check-certificate")
    .build();

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, UnsecureConnectionCheck::checkRun);
  }

  private static void checkRun(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = runInstruction.arguments().stream().map(ArgumentResolution::of).toList();

    SENSITIVE_CURL_COMMAND.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE));
    SENSITIVE_WGET_COMMAND.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE));
  }
}
