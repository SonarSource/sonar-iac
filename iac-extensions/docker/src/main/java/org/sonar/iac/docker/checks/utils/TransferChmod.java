/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.List;
import org.sonar.iac.common.checks.Chmod;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.TransferInstruction;

public record TransferChmod(Flag flag, ArgumentResolution resolution, Chmod chmod) {

  public static List<TransferChmod> extractChmods(TransferInstruction transferInstruction) {
    return transferInstruction.options().stream()
      .filter(flag -> "chmod".equals(flag.name()))
      .map(flag -> {
        var resolution = ArgumentResolution.of(flag.value());
        return new TransferChmod(flag, resolution, Chmod.fromString(resolution.value()));
      })
      .toList();
  }

  public boolean hasPermission(String right) {
    return chmod.hasPermission(right);
  }
}
