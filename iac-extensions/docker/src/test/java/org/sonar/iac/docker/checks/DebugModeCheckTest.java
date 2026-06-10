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
package org.sonar.iac.docker.checks;

import org.junit.jupiter.api.Test;

class DebugModeCheckTest {

  @Test
  void test() {
    DockerVerifier.verify("DebugModeCheckTest/Dockerfile", new DebugModeCheck());
  }

  @Test
  void shouldVerifyCleanDockerfileHasNoIssues() {
    DockerVerifier.verifyContentNoIssue("FROM ubuntu:22.04\n", new DebugModeCheck());
  }

  @Test
  void shouldOnlyFlagStagesInFinalImage() {
    DockerVerifier.verify("DebugModeCheckTest/Dockerfile_multi_stage", new DebugModeCheck());
  }

  @Test
  void shouldNotRaiseForDebugEnvInStagesOutsideFinalImageFromChain() {
    DockerVerifier.verifyNoIssue("DebugModeCheckTest/Dockerfile_multi_stage_fresh_base", new DebugModeCheck());
  }

  @Test
  void shouldNotRaiseForDebugEnvInStageCopiedIntoFinalImage() {
    // ENV does not propagate through COPY --from; only the FROM chain matters
    DockerVerifier.verifyNoIssue("DebugModeCheckTest/Dockerfile_multi_stage_copy_from_debug", new DebugModeCheck());
  }

}
