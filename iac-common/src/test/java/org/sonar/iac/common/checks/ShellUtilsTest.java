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
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ShellUtilsTest {

  @ParameterizedTest
  @ValueSource(strings = {
    "bash",
    "sh",
    "BASH",
    "SH",
    "sH",
    "   bash   ",
    "/bin/bash",
    "/usr/bin/bash",
    "/usr/local/bin/bash --noprofile --norc -euo pipefail",
    "/usr/local/bin/bash --noprofile --norc -euo pipefail {0}",
    // msys2
    "msys2",
    "msys2 {0}",
    "msys2bash.cmd",
    "msys2bash.cmd -c",
    "C:\\shells/msys2bash.cmd {0}",
    // cygwin
    "c:\\cygwin\\bin\\bash",
    "C:\\cygwin\\bin\\bash.exe",
    "C:\\cygwin\\bin\\bash.exe --login '{0}'",
    "C:\\msys64\\usr\\bin\\bash.exe -le {0}",
    // wsl
    "wsl-bash",
    "wsl-bash -c",

  })
  void shouldDetectBashShell(String text) {
    assertThat(ShellUtils.isBashShell(text)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "",
    "powershell",
    "powershell -command",
    "cmd",
    "cmd /S /C",
    " cmd ",
    "pwsh",
    "zsh",
    "fish",
    "/bin/zsh",
    "/usr/bin/fish",
    "/usr/local/bin/zsh --login",
    "/rel/third_party/rez/current/ws9/bin/python3 {0}",
    "chroot-sh {0}",
    // FNs, only first arg is considered
    "nix develop --command bash {0}",
    "/usr/bin/arch /bin/bash {0}",
  })
  void shouldNotDetectBashShell(String text) {
    assertThat(ShellUtils.isBashShell(text)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"-e", "-eu", "-eo", "-euo", "-xe", "-ex"})
  void shouldDetectErrexitOption(String text) {
    assertThat(ShellUtils.isErrexitOption(text)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"-x", "-u", "-o", "--errexit", "errexit", "e", "pipefail", "{0}"})
  void shouldNotDetectErrexitOption(String text) {
    assertThat(ShellUtils.isErrexitOption(text)).isFalse();
  }

  @ParameterizedTest
  @MethodSource
  void shouldFindTheCommandStringOfShellInvocations(List<String> argv, int expected) {
    assertThat(ShellUtils.shellScriptIndex(argv)).isEqualTo(expected);
  }

  static Stream<Arguments> shouldFindTheCommandStringOfShellInvocations() {
    return Stream.of(
      Arguments.of(List.of("sh", "-c", "npm install"), 2),
      Arguments.of(List.of("/bin/bash", "-ec", "npm install"), 2),
      Arguments.of(List.of("bash", "-o", "errexit", "-c", "npm install"), 4),
      Arguments.of(List.of("bash", "-O", "extglob", "-c", "npm install"), 4),
      Arguments.of(List.of("bash", "+o", "posix", "+O", "extglob", "+e", "-c", "npm install"), 7),
      Arguments.of(List.of("bash", "--noprofile", "--norc", "--posix", "-c", "npm install"), 5),
      // Bash reads the command string from the first non-option argument, which may follow other options or "--".
      Arguments.of(List.of("bash", "-c", "-x", "npm install"), 3),
      Arguments.of(List.of("sh", "-c", "--", "npm install"), 3),
      Arguments.of(List.of("sh", "-c", "npm install", "name", "argument"), 2));
  }

  @ParameterizedTest
  @MethodSource
  void shouldNotFindACommandStringWhenInvocationIsMissingOrAmbiguous(List<String> argv) {
    assertThat(ShellUtils.shellScriptIndex(argv)).isEqualTo(-1);
  }

  static Stream<List<String>> shouldNotFindACommandStringWhenInvocationIsMissingOrAmbiguous() {
    return Stream.of(
      List.of(),
      List.of("sh"),
      List.of("sh", "-c"),
      List.of("sh", "-o"),
      List.of("sh", "script.sh"),
      List.of("sh", "-e", "script.sh"),
      List.of("sh", "+c", "npm install"),
      List.of("sh", "-Zc", "npm install"),
      List.of("sh", "-", "-c", "npm install"),
      List.of("bash", "--rcfile", "file", "-c", "npm install"),
      List.of("echo", "-c", "npm install"),
      List.of("bash", "--norc", "script.sh"),
      // isBashShell ignores everything after the first space, so an executable name containing one is not a shell.
      List.of("sh -x", "-c", "npm install"),
      // -n reads commands without running them, and -D only prints translatable strings.
      List.of("sh", "-nc", "npm install"),
      List.of("bash", "-D", "-c", "npm install"),
      // The argument of -o is an option name, so a "-c" in its place is not the command-string flag; Bash rejects it and runs nothing.
      List.of("bash", "-o", "-c", "npm install"));
  }
}
