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
package org.sonar.iac.docker.checks.utils;

import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.checks.utils.StringPredicate.containsIgnoreQuotes;
import static org.sonar.iac.docker.checks.utils.StringPredicate.equalsIgnoreQuotes;
import static org.sonar.iac.docker.checks.utils.StringPredicate.startsWithIgnoreQuotes;

class StringPredicateTest {

  @ParameterizedTest
  @ValueSource(strings = {
    "value",
    "\"value\"",
    "'value'"
  })
  void equalsWithSingleValueIsTrue(String value) {
    Predicate<String> predicate = equalsIgnoreQuotes("value");

    assertThat(predicate.test(value)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    " value",
    "value ",
    " value ",
    "other",
    "valueother",
    "value other",
    "\"value",
    "value\"",
    "'value",
    "value'"
  })
  void equalsWithSingleValueIsFalse(String value) {
    Predicate<String> predicate = equalsIgnoreQuotes("value");

    assertThat(predicate.test(value)).isFalse();
  }

  @Test
  void testMultipleValue() {
    Predicate<String> predicate = containsIgnoreQuotes(Set.of("string", "value"));
    assertThat(predicate.test("string")).isTrue();
    assertThat(predicate.test("\"string\"")).isTrue();
    assertThat(predicate.test("'string'")).isTrue();

    assertThat(predicate.test("value")).isTrue();
    assertThat(predicate.test("\"value\"")).isTrue();
    assertThat(predicate.test("'value'")).isTrue();

    assertThat(predicate.test(" other ")).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "value",
    "\"value\"",
    "'value'",
    "value123",
    "\"value123\"",
    "'value123'"
  })
  void startsWithWithSingleValueIsTrue(String value) {
    Predicate<String> predicate = startsWithIgnoreQuotes("value");

    assertThat(predicate.test(value)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "val",
    "\"val\"",
    "'val'",
    " value",
    " value ",
    "other",
    "\"value",
    "'value"
  })
  void startsWithWithSingleValueIsFalse(String value) {
    Predicate<String> predicate = startsWithIgnoreQuotes("value");

    assertThat(predicate.test(value)).isFalse();
  }
}
