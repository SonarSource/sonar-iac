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
package org.sonar.iac.jvmframeworkconfig.checks.spring;

import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.jvmframeworkconfig.utils.JvmFrameworkConfigVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class ExcessiveFileUploadSizeLimitCheckTest {
  @Test
  void shouldDetectSensitiveValueInProperties() {
    JvmFrameworkConfigVerifier.verify("ExcessiveFileUploadSizeLimitCheck/spring/ExcessiveFileUploadSizeLimitCheck.properties", new ExcessiveFileUploadSizeLimitCheck());
  }

  @Test
  void shouldDetectSensitiveValueInYaml() {
    JvmFrameworkConfigVerifier.verify("ExcessiveFileUploadSizeLimitCheck/spring/ExcessiveFileUploadSizeLimitCheck.yaml", new ExcessiveFileUploadSizeLimitCheck());
  }

  @Test
  void shouldDetectSensitiveValueInPropertiesWithCustomLimit() {
    ExcessiveFileUploadSizeLimitCheck check = new ExcessiveFileUploadSizeLimitCheck();
    check.fileUploadSizeLimit = 1024 * 1024; // 1MB
    JvmFrameworkConfigVerifier.verify("ExcessiveFileUploadSizeLimitCheck/spring/ExcessiveFileUploadSizeLimitCheck-customRuleProperty.properties", check);
  }

  @ParameterizedTest
  @CsvSource(value = {
    "1, 1",
    "1B, 1",
    "1024, 1024",
    "1024B, 1024",
    "1KB, 1024",
    "1MB, 1048576",
    "1GB, 1073741824",
    "5KB, 5120",
    "5MB, 5242880",
    "256MB, 268435456",
    "1TB, 1099511627776",
    "1 byte, null",
    "1 B, null",
    "kilobyte, null",
    "1PB, null",
  }, nullValues = "null")
  void shouldParseSizeSpecifiers(String value, @Nullable Long expected) {
    assertThat(ExcessiveFileUploadSizeLimitCheck.sizeBytes(value)).isEqualTo(expected);
  }
}
