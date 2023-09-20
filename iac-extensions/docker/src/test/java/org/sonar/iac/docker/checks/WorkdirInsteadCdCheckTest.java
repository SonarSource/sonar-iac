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
package org.sonar.iac.docker.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.testing.TemplateFileReader;

class WorkdirInsteadCdCheckTest {

  private final WorkdirInsteadCdCheck check = new WorkdirInsteadCdCheck();

  @Test
  void shouldCheckMixedCommandInstructions() {
    DockerVerifier.verify("WorkdirInsteadCdCheck/workdir_mixed.dockerfile", check);
  }

  @ValueSource(strings = {
    "RUN",
    "CMD",
    "ENTRYPOINT"
  })
  @ParameterizedTest(name = "test issues for command: {0}")
  void issuesRaisedOnTemplateShouldBeCorrect(String instructionName) {
    String[] replacements = new String[] {
      "{$instructionName}", instructionName};

    String content = TemplateFileReader.readTemplateAndReplace("WorkdirInsteadCdCheck/workdir_template.dockerfile", replacements);
    DockerVerifier.verifyContent(content, new WorkdirInsteadCdCheck());
  }
}
