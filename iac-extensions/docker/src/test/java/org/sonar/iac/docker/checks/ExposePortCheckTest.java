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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;

import static org.assertj.core.api.Assertions.assertThat;


class ExposePortCheckTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  ExposePortCheck check = new ExposePortCheck();
  @Test
  void test() {
    DockerVerifier.verify("ExposePortCheck/Dockerfile.default", check);
  }

  @Test
  void custom() {
    check.portList = "100, 200,300";
    DockerVerifier.verify("ExposePortCheck/Dockerfile.custom", check);
  }

  @Test
  void invalid_custom() {
    check.portList = "23, x";
    DockerVerifier.verify("ExposePortCheck/Dockerfile.default", check);
    assertThat(logTester.logs(LoggerLevel.WARN)).contains("The port list provided for ExposePortCheck (S6473) is not a comma seperated list of integers. " +
      "The default list is used. Invalid list of ports \"23, x\"");

  }
}
