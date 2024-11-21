/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.docker.checks.utils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.CommandInstruction;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.HasArguments;
import org.sonar.iac.docker.tree.api.ShellForm;
import org.sonar.iac.docker.tree.api.Variable;

public final class CheckUtils {
  private static final ResolvedCommandPartParser RESOLVED_COMMAND_PART_PARSER = ResolvedCommandPartParser.create();

  private CheckUtils() {
    // utils class
  }

  public static List<ArgumentResolution> resolveInstructionArguments(HasArguments instructionWithArguments) {
    return instructionWithArguments.arguments().stream()
      .map(ArgumentResolution::ofWithoutStrippingQuotes)
      .flatMap(CheckUtils::resolvePartsAfterInitialResolution)
      .toList();
  }

  public static Optional<Flag> getParamByName(Collection<Flag> params, String name) {
    return params.stream().filter(param -> name.equals(param.name())).findFirst();
  }

  public static <T extends CommandInstruction> BiConsumer<CheckContext, T> ignoringHeredoc(BiConsumer<CheckContext, T> visitor) {
    return (ctx, commandInstruction) -> {
      if (DockerTree.Kind.HEREDOCUMENT != commandInstruction.getKindOfArgumentList()) {
        visitor.accept(ctx, commandInstruction);
      }
    };
  }

  /**
   * Only execute the visitor if this CommandInstruction is not an ExecForm.
   * <br/>
   * Unlike the shell form, the exec form does not invoke a command shell. This means that normal shell processing does not happen.
   * Thus, checks about shell best practices are not applicable to commands in ExecForm.
   */
  public static <T extends CommandInstruction> BiConsumer<CheckContext, T> ignoringExecForm(BiConsumer<CheckContext, T> visitor) {
    return (ctx, commandInstruction) -> {
      if (DockerTree.Kind.EXEC_FORM != commandInstruction.getKindOfArgumentList()) {
        visitor.accept(ctx, commandInstruction);
      }
    };
  }

  public static boolean isScratchImage(String imageName) {
    return "scratch".equals(imageName);
  }

  private static Stream<ArgumentResolution> resolvePartsAfterInitialResolution(ArgumentResolution argument) {
    var expressions = argument.argument().expressions();
    if (argument.isResolved() && !argument.value().isBlank() && expressions.stream().anyMatch(Variable.class::isInstance)) {
      // If the argument represents a resolved variable, it may in fact be a part of the command and not just a single value.
      var shellForm = (ShellForm) RESOLVED_COMMAND_PART_PARSER.parseWithTextRange(
        TextRanges.merge(argument.argument().expressions().stream().map(DockerTree::textRange).filter(Objects::nonNull).toList()),
        " " + argument.value());
      return shellForm.arguments().stream().map(ArgumentResolution::ofWithoutStrippingQuotes);
    } else {
      return Stream.of(argument);
    }
  }
}
