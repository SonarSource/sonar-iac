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
package org.sonar.iac.kubernetes;

import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.iac.common.testing.TextRangePluginApiAssert;
import org.sonar.iac.helm.tree.api.Location;
import org.sonar.iac.helm.tree.impl.LocationAssert;

public class KubernetesAssertions {

  public static LocationAssert assertThat(Location actual) {
    return LocationAssert.assertThat(actual);
  }

  public static TextRangeAssert assertThat(TextRange actual) {
    return TextRangeAssert.assertThat(actual);
  }

  public static TextRangePluginApiAssert assertThat(@Nullable org.sonar.api.batch.fs.TextRange actual) {
    return TextRangePluginApiAssert.assertThat(actual);
  }
}
