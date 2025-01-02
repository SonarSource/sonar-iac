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
package org.sonar.iac.jvmframeworkconfig;

import javax.annotation.Nullable;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.testing.ExternalIssueAssert;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.iac.common.testing.TextRangePluginApiAssert;

public class JvmFrameworkConfigAssertions {
  public static TextRangeAssert assertThat(@Nullable TextRange actual) {
    return TextRangeAssert.assertThat(actual);
  }

  public static TextRangePluginApiAssert assertThat(@Nullable org.sonar.api.batch.fs.TextRange actual) {
    return TextRangePluginApiAssert.assertThat(actual);
  }

  public static ExternalIssueAssert assertThat(@Nullable ExternalIssue issue) {
    return ExternalIssueAssert.assertThat(issue);
  }
}
