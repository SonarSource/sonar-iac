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

import javax.annotation.Nullable;
import org.sonar.iac.docker.tree.api.Argument;
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

  public static void assertKeyValuePair(NewKeyValuePair keyValuePair, String expectedKey, @Nullable String expectedValue) {
    assertThat(argValue(keyValuePair.key())).isEqualTo(expectedKey);
    if (expectedValue == null) {
      assertThat(keyValuePair.value()).isNull();
    } else {
      assertThat(argValue(keyValuePair.value())).isEqualTo(expectedValue);
    }
  }
}
