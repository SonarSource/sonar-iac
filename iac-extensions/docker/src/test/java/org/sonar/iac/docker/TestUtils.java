/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker;

import java.util.Objects;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.NewKeyValuePair;
import org.sonar.iac.docker.utils.ArgumentUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

  private TestUtils() {
    // utils class
  }

  @Nullable
  public static String argValue(Argument argument) {
    return ArgumentUtils.resolve(argument).value();
  }

  public static void assertFrom(FromInstruction from, String expectedName, @Nullable String expectedFlagName, @Nullable String expectedFlagValue, @Nullable String expectedAlias) {
    assertThat(argValue(from.image())).isEqualTo(expectedName);
    if (expectedFlagName != null) {
      assertThat(from.platform()).isNotNull();
      assertThat(from.platform().name()).isEqualTo(expectedFlagName);
      if (expectedFlagValue != null) {
        assertThat(argValue(from.platform().value())).isEqualTo(expectedFlagValue);
      } else {
        assertThat(from.platform().value()).isNull();
      }
    } else {
      assertThat(from.platform()).isNull();
    }

    if (expectedAlias != null) {
      assertThat(from.alias().alias().value()).isEqualTo(expectedAlias);
    } else {
      assertThat(from.alias()).isNull();
    }
  }
}
