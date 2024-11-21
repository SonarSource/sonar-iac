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
package org.sonar.iac.arm.checks.utils;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.tree.impl.bicep.StringLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.SyntaxTokenImpl;

class CheckUtilsTest {
  @ParameterizedTest
  @CsvSource({
    "2021-01-01, 2021-01-01, true",
    "2021-01-01, 2021-03-01, false",
    "2021-01-01, 2021-01-03, false",
    "2023-01-01, 2021-01-03, true",
    "2021-03-01, 2021-01-03, true",
    "2021-01-03, 2021-01-03, true",
    "2021-12-31, 2021-01-01, true",
    "2021-01-01-preview, 2021-01-01, false",
    "2021-01-01, 2021-01-01-preview, true",
    "2021-02-01-preview, 2021-01-01, true",
    "2021-02-01-preview, 2021-01-01-preview, true",
    "1, 2, false",
    "1, 2021-01-01, false",
  })
  void shouldCompareVersions(String version, String targetVersion, boolean shouldBeGreaterThanTarget) {
    var expression = new StringLiteralImpl(
      new SyntaxTokenImpl('"' + version + '"', null, List.of()));
    var predicate = CheckUtils.isVersionNewerOrEqualThan(targetVersion);

    Assertions.assertThat(predicate.test(expression)).isEqualTo(shouldBeGreaterThanTarget);
  }
}
