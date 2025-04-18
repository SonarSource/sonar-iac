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
package org.sonar.iac.docker.checks.utils.command;

import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.checks.utils.command.StringPredicate.containsIgnoreQuotes;
import static org.sonar.iac.docker.checks.utils.command.StringPredicate.equalsIgnoreQuotes;
import static org.sonar.iac.docker.checks.utils.command.StringPredicate.startsWithIgnoreQuotes;

class StringPredicateTest {

  @ParameterizedTest
  @ValueSource(strings = {
    "value",
    "\"value\"",
    "'value'"
  })
  void equalsWithSingleValueShouldBeTrue(String value) {
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
  void equalsWithSingleValueShouldBeFalse(String value) {
    Predicate<String> predicate = equalsIgnoreQuotes("value");

    assertThat(predicate.test(value)).isFalse();
  }

  @Test
  void shouldMatchMultipleValues() {
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
  void startsWithWithSingleValueShouldBeTrue(String value) {
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
  void startsWithWithSingleValueShouldBeFalse(String value) {
    Predicate<String> predicate = startsWithIgnoreQuotes("value");

    assertThat(predicate.test(value)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "value",
    "\"value\"",
    "'value'",
    "value123",
    "\"value123\"",
    "'value123'",
    "expected",
    "expected=123",
    "expected-value",
    "value-expected"
  })
  void shouldStartsWithIgnoreQuotesListShouldBeTrue(String value) {
    Predicate<String> predicate = startsWithIgnoreQuotes("value", "expected");

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
    "'value",
    "exp",
    "--expected",
    "'expected",
    "ex-pected"
  })
  void startsStartsWithIgnoreQuotesListShouldBeFalse(String value) {
    Predicate<String> predicate = startsWithIgnoreQuotes("value", "expected");

    assertThat(predicate.test(value)).isFalse();
  }
}
