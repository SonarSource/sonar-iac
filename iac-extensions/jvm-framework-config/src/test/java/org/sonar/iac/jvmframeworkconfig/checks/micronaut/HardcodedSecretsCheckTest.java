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
package org.sonar.iac.jvmframeworkconfig.checks.micronaut;

import org.junit.jupiter.api.Test;
import org.sonar.iac.jvmframeworkconfig.utils.JvmFrameworkConfigVerifier;

class HardcodedSecretsCheckTest {
  @Test
  void shouldDetectSensitiveValueInProperties() {
    JvmFrameworkConfigVerifier.verify("HardcodedSecretsCheck/micronaut/HardcodedSecretsCheck.properties", new HardcodedSecretsCheck());
  }

  @Test
  void shouldDetectSensitiveValueInYaml() {
    JvmFrameworkConfigVerifier.verify("HardcodedSecretsCheck/micronaut/HardcodedSecretsCheck.yaml", new HardcodedSecretsCheck());
  }
}
