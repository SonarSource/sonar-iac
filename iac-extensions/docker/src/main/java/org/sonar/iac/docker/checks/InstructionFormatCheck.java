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

import java.util.Locale;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.Alias;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.Instruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

@Rule(key = "S6476")
public class InstructionFormatCheck implements IacCheck {

  private static final String MESSAGE = "Replace \"%s\" with upper case format \"%s\".";
  private static final Pattern KEYWORD_PATTERN = Pattern.compile("[a-z]");

  @Override
  public void initialize(InitContext init) {
    init.register(Instruction.class, (ctx, instruction) -> checkInstructionKeyword(ctx, instruction.keyword()));

    init.register(FromInstruction.class, (ctx, from) -> {
      Alias alias = from.alias();
      if (alias != null) {
        checkInstructionKeyword(ctx, alias.keyword());
      }
    });
  }

  private static void checkInstructionKeyword(CheckContext ctx, SyntaxToken keyword) {
    String value = keyword.value();
    if (KEYWORD_PATTERN.matcher(value).find()) {
      ctx.reportIssue(keyword, String.format(MESSAGE, value, value.toUpperCase(Locale.ROOT)));
    }
  }

}
