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
package org.sonar.iac.docker.checks;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.assertj.core.api.Assertions.assertThat;

class RetrieveRemoteResourcesCheckTest {

  RetrieveRemoteResourcesCheck check = new RetrieveRemoteResourcesCheck();

  @Test
  void shouldVerifyWget() {
    DockerVerifier.verify("RetrieveRemoteResourcesCheck/wget.dockerfile", check);
  }

  @Test
  void shouldVerifyCurl() {
    DockerVerifier.verify("RetrieveRemoteResourcesCheck/curl.dockerfile", check);
  }

  @Test
  void shouldReturnZeroForEmptyArguments() {
    List<ArgumentResolution> args = List.of();
    var result = RetrieveRemoteResourcesCheck.findPositionOf(args, "foo");
    assertThat(result).isZero();
  }
}
