/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.checks;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class UnencryptedProtocolCheckTest {

  // TODO : SONARIAC-572 and SONARIAC-541 to be completed before switching to arguments() instead of literals
  @Disabled("To enable back when transition is done and literals() has been replaced by arguments()")
  @Test
  void test() {
    DockerVerifier.verify("UnencryptedProtocolCheck/Dockerfile", new UnencryptedProtocolCheck());
  }

}
