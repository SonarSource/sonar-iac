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
package org.sonar.iac.helm.jna;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoaderTest {
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
    var loader = new Loader();
    assertEquals(expected, loader.getNormalizedOsName(osName));
  }

  @Test
  void shouldThrowOnUnknownOs() {
    var loader = new Loader();
    Assertions.assertThatThrownBy(() -> loader.getNormalizedOsName("freebsd"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Unsupported OS: freebsd");
  }
}
