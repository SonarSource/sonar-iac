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
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ShellForm;

@Rule(key = "S7030")
public class MalformedJsonInExecCheck implements IacCheck {

  private static final String MESSAGE = "Fix this invalid JSON to prevent unexpected behavior of the exec form.";
  // Exclude pattern: if there is a lot of characters after the closing bracket, then we consider it's not a malformed Exec form
  private static final Predicate<String> EXCLUDE_TOO_LONG_AFTER_OBJECT = Pattern.compile("^\\[[^]]+][^\n\r]{10,}").asMatchPredicate();
  // Exclude pattern: if there is no quote-like characters inside the brackets, we consider it's not a malformed Exec form
  private static final Predicate<String> EXCLUDE_NO_QUOTES = Pattern.compile("^\\[[^]'\"\\p{Pi}\\p{Pf}]+]").asPredicate();

  @Override
  public void initialize(InitContext init) {
    init.register(ShellForm.class, MalformedJsonInExecCheck::checkShellForm);
  }

  private static void checkShellForm(CheckContext ctx, ShellForm shellForm) {
    if (isMalformedExec(shellForm)) {
      ctx.reportIssue(shellForm, MESSAGE);
    }
  }

  private static boolean isMalformedExec(ShellForm shellForm) {
    String command = resolveFullCommand(shellForm.arguments());
    return command != null && command.startsWith("[") && !EXCLUDE_TOO_LONG_AFTER_OBJECT.test(command) && !EXCLUDE_NO_QUOTES.test(command);
  }

  /**
   * Resolve all provided arguments.
   * If at least one of them is {@link ArgumentResolution.Status#UNRESOLVED}, then return null instead.
   */
  @Nullable
  private static String resolveFullCommand(List<Argument> arguments) {
    List<ArgumentResolution> resolved = arguments.stream()
      .map(ArgumentResolution::ofWithoutStrippingQuotes)
      .toList();
    if (resolved.stream().anyMatch(ArgumentResolution::isUnresolved)) {
      return null;
    }
    return resolved.stream().map(ArgumentResolution::value).collect(Collectors.joining());
  }
}
