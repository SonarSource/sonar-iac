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
package org.sonar.iac.docker.checks;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutableNotOwnedByRootCheckTest {

  @Test
  void test_add_copy() {
    DockerVerifier.verify("ExecutableNotOwnedByRootCheck/add_copy.dockerfile", new ExecutableNotOwnedByRootCheck());
  }

  private static Stream<Arguments> provideArgumentsForNonRootAtIdTest() {
    int USER_INDEX = 0;
    int GROUP_INDEX = 1;
    return Stream.of(
      Arguments.of("", USER_INDEX, false),
      Arguments.of("", GROUP_INDEX, false),
      Arguments.of(":", USER_INDEX, false),
      Arguments.of(":", GROUP_INDEX, false),
      Arguments.of(":x", USER_INDEX, false),
      Arguments.of(":x", GROUP_INDEX, true),
      Arguments.of(":root", USER_INDEX, false),
      Arguments.of(":root", GROUP_INDEX, false),
      Arguments.of("x:", USER_INDEX, true),
      Arguments.of("x:", GROUP_INDEX, false),
      Arguments.of("root:", USER_INDEX, false),
      Arguments.of("root:", GROUP_INDEX, false),
      Arguments.of("root:x", USER_INDEX, false),
      Arguments.of("root:x", GROUP_INDEX, true),
      Arguments.of("x:root", USER_INDEX, true),
      Arguments.of("x:root", GROUP_INDEX, false),
      Arguments.of("root:root", USER_INDEX, false),
      Arguments.of("root:root", GROUP_INDEX, false),
      Arguments.of("x:x", USER_INDEX, true),
      Arguments.of("x:x", GROUP_INDEX, true));
  }

  @ParameterizedTest
  @MethodSource("provideArgumentsForNonRootAtIdTest")
  void nonRootAtIdTest(String chownValue, int indexToCheck, boolean expectedResult) {
    boolean isNonRootAtId = ExecutableNotOwnedByRootCheck.isNonRootAtId(chownValue, indexToCheck);
    assertThat(isNonRootAtId).isEqualTo(expectedResult);
  }
}
