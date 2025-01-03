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

import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6431")
public class HostNetworkNamespaceCheck implements IacCheck {

  private static final String MESSAGE = "Make sure it is safe to use the host operating system namespace here.";

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, HostNetworkNamespaceCheck::checkHostNetwork);
  }

  private static void checkHostNetwork(CheckContext ctx, RunInstruction runInstruction) {
    runInstruction.options().stream()
      .filter(flag -> "network".equals(flag.name()) && isArgValue(flag.value(), "host"))
      .forEach(flag -> ctx.reportIssue(flag, MESSAGE));
  }

  private static boolean isArgValue(@Nullable Argument argument, String expectedValue) {
    return argument != null && expectedValue.equals(ArgumentResolution.of(argument).value());
  }
}
