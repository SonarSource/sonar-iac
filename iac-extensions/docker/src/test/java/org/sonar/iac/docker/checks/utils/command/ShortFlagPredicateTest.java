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
package org.sonar.iac.docker.checks.utils.command;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.checks.utils.command.StandardCommandDetectors.shortFlagPredicate;

class ShortFlagPredicateTest {

  ShortFlagPredicate predicate = new ShortFlagPredicate('X');
  ShortFlagPredicate predicateCreatedByStaticMethod = shortFlagPredicate('X');

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
    assertThat(predicateCreatedByStaticMethod.test(argument)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "-",
    "--",
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
    assertThat(predicateCreatedByStaticMethod.test(argument)).isFalse();
  }
}
