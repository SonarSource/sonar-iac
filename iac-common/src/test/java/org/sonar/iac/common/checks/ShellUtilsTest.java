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

import org.junit.jupiter.params.ParameterizedTest;
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
}
