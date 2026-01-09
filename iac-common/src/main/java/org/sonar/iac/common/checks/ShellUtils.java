/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.common.checks;

import java.util.Locale;
import java.util.Set;

public final class ShellUtils {

  private ShellUtils() {
    // utility class
  }

  /**
   * List of shells that execute Bash code => we need to parse it.
   */
  private static final Set<String> SUPPORTED_SHELLS = Set.of("bash", "bash.exe", "sh", "msys2", "wsl-bash", "msys2bash.cmd");

  public static boolean isBashShell(String shell) {
    var normalizedShell = getNormalizedShellCommand(shell);
    return SUPPORTED_SHELLS.contains(normalizedShell);
  }

  private static String getNormalizedShellCommand(String shell) {
    var normalizedShell = shell.toLowerCase(Locale.ROOT).trim();
    if (normalizedShell.contains(" ")) {
      // trim options
      normalizedShell = normalizedShell.substring(0, normalizedShell.indexOf(' '));
    }
    if (normalizedShell.contains("/")) {
      // trim path and take only command name
      normalizedShell = normalizedShell.substring(normalizedShell.lastIndexOf('/') + 1);
    }
    // Support also Windows paths
    if (normalizedShell.contains("\\")) {
      normalizedShell = normalizedShell.substring(normalizedShell.lastIndexOf('\\') + 1);
    }
    return normalizedShell;
  }
}
