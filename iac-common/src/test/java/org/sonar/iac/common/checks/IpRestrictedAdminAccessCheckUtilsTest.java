/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.checks;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils.rangeContainsSshOrRdpPort;

class IpRestrictedAdminAccessCheckUtilsTest {

  @Test
  void testSimplePort() {
    assertThat(rangeContainsSshOrRdpPort("*")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("22")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("3389")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("80")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("222")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("other")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("-22")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("**")).isFalse();
  }

  @Test
  void testPortRange() {
    assertThat(rangeContainsSshOrRdpPort("10-30")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("22-22")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("10-4000")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("500-4000")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("5-20")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("25-300")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("3400-3500")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("3400-string")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("10-22-50")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("*-22")).isFalse();
  }
}
