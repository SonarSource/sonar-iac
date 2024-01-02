/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.FromInstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.DockerAssertions.assertThat;

/**
 * Define static methods that can be used to help writing tests.
 */
public class TestUtils {

  private TestUtils() {
    // utils class
  }

  public static void assertArgumentsValue(List<Argument> args, String... values) {
    assertThat(args).hasSize(values.length);
    for (int i = 0; i < args.size(); i++) {
      assertThat(args.get(i)).hasValue(values[i]);
    }
  }

  public static void assertFrom(FromInstruction from, String expectedName, @Nullable String expectedFlagName, @Nullable String expectedFlagValue, @Nullable String expectedAlias) {
    assertThat(from.image()).hasValue(expectedName);
    if (expectedFlagName != null) {
      assertThat(from.platform()).isNotNull();
      assertThat(from.platform().name()).isEqualTo(expectedFlagName);
      if (expectedFlagValue != null) {
        assertThat(from.platform().value()).hasValue(expectedFlagValue);
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
