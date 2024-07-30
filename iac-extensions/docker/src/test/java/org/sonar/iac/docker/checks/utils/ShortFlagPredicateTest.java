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
package org.sonar.iac.docker.checks.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ShortFlagPredicateTest {

  ShortFlagPredicate predicate = new ShortFlagPredicate('X');

  @ParameterizedTest
  @ValueSource(strings = {
    "-X",
    "-aX",
    "-Xa",
    "-aXa",
    "-Xab",
    "-abX",
    "-abXcd",
    "-aaXaa",
    "-aXaXa",
  })
  void shouldPredicateBeTrue(String argument) {
    assertThat(predicate.test(argument)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "--X",
    "--Xabcd",
    "--abcdX",
    "-a",
    "-abc",
    "-x",
    "-xyz",
    "-ABC"
  })
  void shouldPredicateBeFalse(String argument) {
    assertThat(predicate.test(argument)).isFalse();
  }
}
