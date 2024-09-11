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

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.RunInstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class AbstractEnvVariableMonitorCheckTest {
  @Test
  void shouldHaveVariable() {
    assertOnRun("""
      FROM scratch
      ENV MY_VAR=value
      RUN
      """, (Map<String, String> variables) -> assertThat(variables)
      .containsExactly(
        entry("MY_VAR", "value")));
  }

  @Test
  void shouldHaveNoVariables() {
    assertOnRun("""
      FROM scratch
      RUN
      """, (Map<String, String> variables) -> assertThat(variables).isEmpty());
  }

  @Test
  void shouldOverrideVariableValue() {
    assertOnRun("""
      FROM scratch
      ENV MY_VAR=value1
      ENV MY_VAR=value2
      RUN
      """, (Map<String, String> variables) -> assertThat(variables)
      .containsExactly(
        entry("MY_VAR", "value2")));
  }

  @Test
  void shouldHaveMultipleVariables() {
    assertOnRun("""
      FROM scratch
      ENV MY_VAR_1=value1
      ENV MY_VAR_2=value2
      RUN
      """, (Map<String, String> variables) -> assertThat(variables)
      .containsExactly(
        entry("MY_VAR_1", "value1"),
        entry("MY_VAR_2", "value2")));
  }

  @Test
  void shouldHaveOnlyVariableFromCurrentImageScope() {
    assertOnRun("""
      FROM scratch
      ENV MY_VAR_1=value1
      FROM scratch
      ENV MY_VAR_2=value2
      RUN
      """, (Map<String, String> variables) -> assertThat(variables)
      .containsExactly(
        entry("MY_VAR_2", "value2")));
  }

  @Test
  void testUnresolvedVariableName() {
    assertOnRun("""
      FROM scratch
      ENV $NAME=value
      RUN
      """, (Map<String, String> variables) -> assertThat(variables).isEmpty());
  }

  void assertOnRun(String code, EnvVariableChecker checker) {
    DockerVerifier.verifyContentNoIssue(code, new RunInstructionVisitor(checker));
  }

  static class RunInstructionVisitor extends AbstractEnvVariableMonitorCheck {
    private final EnvVariableChecker envVariableChecker;

    RunInstructionVisitor(EnvVariableChecker envVariableChecker) {
      this.envVariableChecker = envVariableChecker;
    }

    @Override
    public void init(InitContext init) {
      init.register(RunInstruction.class, this::onRunInstruction);
    }

    private void onRunInstruction(CheckContext checkContext, RunInstruction runInstruction) {
      envVariableChecker.checkVariables(getGlobalEnvironmentVariables());
    }
  }

  interface EnvVariableChecker {
    void checkVariables(Map<String, String> variables);
  }
}
