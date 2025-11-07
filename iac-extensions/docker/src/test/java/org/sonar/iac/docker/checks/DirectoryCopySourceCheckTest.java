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
package org.sonar.iac.docker.checks;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class DirectoryCopySourceCheckTest {

  @Test
  void shouldExecuteCheckOnAddInstruction() {
    DockerVerifier.verify("DirectoryCopySourceCheck/Dockerfile_add", new DirectoryCopySourceCheck());
  }

  @Test
  void shouldExecuteCheckOnCopyInstruction() {
    DockerVerifier.verify("DirectoryCopySourceCheck/Dockerfile_copy", new DirectoryCopySourceCheck());
  }

  @MethodSource
  @ParameterizedTest(name = "Should normalize \"{0}\"")
  void shouldNormalizePath(String path, String[] expectedNormalizedResult) {
    assertThat(DirectoryCopySourceCheck.normalize(path)).isEqualTo(expectedNormalizedResult);
  }

  private static Stream<Arguments> shouldNormalizePath() {
    return Stream.of(
      Arguments.of("./test", new String[] {".", "test"}),
      Arguments.of("./p/../test", new String[] {".", "test"}),
      Arguments.of("/test", new String[] {"", "test"}),
      Arguments.of("/./test", new String[] {"", "test"}),
      Arguments.of("test", new String[] {"test"}),
      Arguments.of("test/p", new String[] {"test", "p"}),
      Arguments.of("c:/test", new String[] {"c:", "test"}),
      Arguments.of("./test/a*", new String[] {".", "test", "a*"}));
  }
}
