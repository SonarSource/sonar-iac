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
package org.sonar.iac.common.checks;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class ShellUtils {

  private ShellUtils() {
    // utility class
  }

  /**
   * List of shells that execute Bash code => we need to parse it.
   */
  private static final Set<String> SUPPORTED_SHELLS = Set.of("bash", "bash.exe", "sh", "msys2", "wsl-bash", "msys2bash.cmd");
  // Single-letter options that neither take an argument nor stop execution (-n reads without running, -D only prints strings).
  private static final Pattern SHORT_OPTIONS = Pattern.compile("-[abcefhiklmprstuvxBCEHPT]+|\\+[abefhiklmprstuvxBCEHPT]+");
  private static final Set<String> OPTIONS_WITH_ARGUMENT = Set.of("-o", "+o", "-O", "+O");
  private static final Set<String> LONG_OPTIONS = Set.of("--noprofile", "--norc", "--posix");

  public static boolean isBashShell(String shell) {
    var normalizedShell = getNormalizedShellCommand(shell);
    return SUPPORTED_SHELLS.contains(normalizedShell);
  }

  /**
   * Whether a single shell option token enables errexit, e.g. {@code -e}, {@code -eu} or {@code -eo}.
   * Only matches short, dash-prefixed option combinations; it does not recognize the long form
   * {@code --errexit} or the two-token {@code -o errexit} form.
   */
  public static boolean isErrexitOption(String token) {
    return token.startsWith("-") && !token.startsWith("--") && token.contains("e");
  }

  /**
   * Returns the index of the command string run by {@code sh -c} or {@code bash -c} in the given arguments, or -1 if there is none.
   * Unknown options are rejected because their arity could change which argument is the command string.
   * See <a href="https://www.gnu.org/software/bash/manual/html_node/Invoking-Bash.html">Invoking Bash</a>.
   */
  public static int shellScriptIndex(List<String> argv) {
    if (argv.isEmpty() || argv.get(0).chars().anyMatch(Character::isWhitespace) || !isBashShell(argv.get(0))) {
      return -1;
    }

    boolean commandString = false;
    int index = 1;
    while (index < argv.size() && isOption(argv.get(index))) {
      var option = argv.get(index);
      if (OPTIONS_WITH_ARGUMENT.contains(option)) {
        // The option name that follows is an argument, not an option: without skipping it, "bash -o pipefail -c script" stops at "pipefail",
        // and in "bash -o -c script" the "-c" would be misread as the command-string flag although Bash rejects it as an option name.
        index += 2;
      } else if (SHORT_OPTIONS.matcher(option).matches() || LONG_OPTIONS.contains(option)) {
        commandString |= option.startsWith("-") && !option.startsWith("--") && option.contains("c");
        index++;
      } else {
        return -1;
      }
    }

    if (index < argv.size() && "--".equals(argv.get(index))) {
      index++;
    }

    return commandString && index < argv.size() ? index : -1;
  }

  private static boolean isOption(String argument) {
    return (argument.startsWith("-") || argument.startsWith("+")) && !"--".equals(argument);
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
