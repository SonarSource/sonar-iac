/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;

class SecretsHandlingCheckTest {

  final IacCheck check = new SecretsHandlingCheck();

  @Test
  void test_env() {
    DockerVerifier.verify("SecretsHandlingCheck/SecretsHandlingCheck_env.dockerfile", check);
  }

  @Test
  void test_arg() {
    DockerVerifier.verify("SecretsHandlingCheck/SecretsHandlingCheck_arg.dockerfile", check);
  }

  @Test
  void alternative_escaping() {
    DockerVerifier.verify("SecretsHandlingCheck/SecretsHandlingCheck_alternative_escaping.dockerfile", check);
  }
}
