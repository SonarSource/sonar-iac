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
package org.sonar.iac.docker.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;

class ExposedAdministrationServicesCheckTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  ExposedAdministrationServicesCheck check = new ExposedAdministrationServicesCheck();

  @Test
  void test() {
    DockerVerifier.verify("ExposedAdministrationServicesCheck/Dockerfile.default", check);
  }

  @Test
  void custom() {
    check.portList = "100, 200,300";
    DockerVerifier.verify("ExposedAdministrationServicesCheck/Dockerfile.custom", check);
  }

  @Test
  void invalid_custom() {
    check.portList = "23, x";
    DockerVerifier.verify("ExposedAdministrationServicesCheck/Dockerfile.default", check);
    assertThat(logTester.logs(Level.WARN)).contains("The port list provided for ExposedAdministrationServicesCheck (S6473) is not a comma seperated list of integers. " +
      "The default list is used. Invalid list of ports \"23, x\"");

  }
}
