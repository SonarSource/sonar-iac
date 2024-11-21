/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.checks;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;

class CommandExecutionCheckTest {

  IacCheck check = new CommandExecutionCheck();

  static Stream<String> sensitiveKinds() {
    return Stream.of("Role", "ClusterRole");
  }

  @MethodSource("sensitiveKinds")
  @ParameterizedTest(name = "[{index}] should check command execution for kind: \"{0}\"")
  void shouldCheckCommandExecutionInKind(String kind) {
    String content = readTemplateAndReplace("CommandExecutionCheck/commandExecutionTestTemplate.yaml", kind);
    KubernetesVerifier.verifyContent(content, check);
  }
}
