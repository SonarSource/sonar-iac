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
package org.sonar.iac.docker.checks.utils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.HasArguments;
import org.sonar.iac.docker.tree.api.RunInstruction;

public final class CheckUtils {

  private CheckUtils() {
    // utils class
  }

  public static List<ArgumentResolution> resolveInstructionArguments(HasArguments instructionWithArguments) {
    return instructionWithArguments.arguments().stream().map(ArgumentResolution::ofWithoutStrippingQuotes).collect(Collectors.toList());
  }

  public static String getFileExtension(String name) {
    int lastIndexOf = name.lastIndexOf(".");
    if (lastIndexOf == -1) {
      // empty extension
      return "";
    }
    return name.substring(lastIndexOf + 1);
  }

  public static Optional<Flag> getParamByName(Collection<Flag> params, String name) {
    return params.stream().filter(param -> name.equals(param.name())).findFirst();
  }

  public static BiConsumer<CheckContext, RunInstruction> ignoringHeredoc(BiConsumer<CheckContext, RunInstruction> visitor) {
    return (ctx, runInstruction) -> {
      if (DockerTree.Kind.HEREDOCUMENT != runInstruction.getKindOfArgumentList()) {
        visitor.accept(ctx, runInstruction);
      }
    };
  }
}
