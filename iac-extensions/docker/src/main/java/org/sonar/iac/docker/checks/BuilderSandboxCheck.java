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

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6502")
public class BuilderSandboxCheck implements IacCheck {

  private static final String MESSAGE = "Make sure that disabling the builder sandbox is safe here.";

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, (ctx, instruction) -> {
      for (Flag option : instruction.options()) {
        if ("security".equals(option.name()) && "insecure".equals(ArgumentResolution.of(option.value()).value())) {
          ctx.reportIssue(option, MESSAGE);
        }
      }
    });
  }
}
