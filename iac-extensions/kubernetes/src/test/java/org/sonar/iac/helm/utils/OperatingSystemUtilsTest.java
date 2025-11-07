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
package org.sonar.iac.helm.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperatingSystemUtilsTest {
  @ParameterizedTest
  @CsvSource({
    "linux,linux",
    "darwin,darwin",
    "macosx,darwin",
    "Windows 10,windows",
    "Windows Server 2019,windows",
    "Windows Server 2019 Datacenter,windows",
  })
  void shouldNormalizeOsNames(String osName, String expected) {
    assertEquals(expected, OperatingSystemUtils.getNormalizedOsName(osName));
  }

  @Test
  void shouldThrowOnUnknownOs() {
    Assertions.assertThatThrownBy(() -> OperatingSystemUtils.getNormalizedOsName("freebsd"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Unsupported OS: freebsd");
  }

  @ParameterizedTest
  @CsvSource({
    "amd64,amd64",
    "x86_64,amd64",
    "aarch64,arm64",
    "arm64,arm64",
  })
  void shouldNormalizeArchNames(String archName, String expected) {
    assertEquals(expected, OperatingSystemUtils.getNormalizedArchName(archName));
  }

  @Test
  void shouldThrowOnUnknownArch() {
    Assertions.assertThatThrownBy(() -> OperatingSystemUtils.getNormalizedArchName("arm"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Unsupported architecture: arm");
  }
}
