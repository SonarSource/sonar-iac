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
package org.sonar.iac.springconfig.checks;

import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.springconfig.utils.SpringConfigVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class ExcessiveFileUploadSizeLimitCheckTest {
  @Test
  void shouldDetectSensitiveValueInProperties() {
    SpringConfigVerifier.verify("ExcessiveFileUploadSizeLimitCheck/ExcessiveFileUploadSizeLimitCheck.properties", new ExcessiveFileUploadSizeLimitCheck());
  }

  @Test
  void shouldDetectSensitiveValueInYaml() {
    SpringConfigVerifier.verify("ExcessiveFileUploadSizeLimitCheck/ExcessiveFileUploadSizeLimitCheck.yaml", new ExcessiveFileUploadSizeLimitCheck());
  }

  @Test
  void shouldDetectSensitiveValueInPropertiesWithCustomLimit() {
    ExcessiveFileUploadSizeLimitCheck check = new ExcessiveFileUploadSizeLimitCheck();
    check.fileUploadSizeLimit = 1024 * 1024; // 1MB
    SpringConfigVerifier.verify("ExcessiveFileUploadSizeLimitCheck/ExcessiveFileUploadSizeLimitCheck-customRuleProperty.properties", check);
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
