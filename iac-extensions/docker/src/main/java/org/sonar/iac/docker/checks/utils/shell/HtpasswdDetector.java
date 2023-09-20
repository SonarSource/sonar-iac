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
package org.sonar.iac.docker.checks.utils.shell;

import java.util.List;
import org.sonar.iac.docker.symbols.ArgumentResolution;

public final class HtpasswdDetector {
  private HtpasswdDetector() {
  }

  public static boolean detect(List<ArgumentResolution> resolvedArgument) {
    var flagB = false;
    var flagN = false;
    var numberOfNonFlags = 0;
    for (var i = 0; i < resolvedArgument.size(); i++) {
      String current = resolvedArgument.get(i).value();
      if (i == 0 && !"htpasswd".equals(current)) {
        break;
      }
      if (current.startsWith("-")) {
        if (current.contains("b")) {
          flagB = true;
        }
        if (current.contains("n")) {
          flagN = true;
        }
      } else {
        numberOfNonFlags++;
      }
    }
    return detectedSensitiveCommand(flagB, flagN, numberOfNonFlags);
  }

  private static boolean detectedSensitiveCommand(boolean flagB, boolean flagN, int numberOfNonFlags) {
    return flagB && (notFlagNAnd4NonFlags(flagN, numberOfNonFlags) || flagNAnd3NonFlags(flagN, numberOfNonFlags));
  }

  private static boolean flagNAnd3NonFlags(boolean flagN, int numberOfNonFlags) {
    return flagN && numberOfNonFlags == 3;
  }

  private static boolean notFlagNAnd4NonFlags(boolean flagN, int numberOfNonFlags) {
    return !flagN && numberOfNonFlags == 4;
  }
}
