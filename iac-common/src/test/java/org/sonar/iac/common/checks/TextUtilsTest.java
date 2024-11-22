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
package org.sonar.iac.common.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTextTree.text;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTree.tree;

class TextUtilsTest {

  @Test
  void getValue() {
    assertThat(TextUtils.getValue(text("foo"))).isPresent().get().isEqualTo("foo");
    assertThat(TextUtils.getValue(tree())).isNotPresent();
    assertThat(TextUtils.getValue(null)).isNotPresent();
  }

  @Test
  void getIntValue() {
    assertThat(TextUtils.getIntValue(text("1"))).isPresent().get().isEqualTo(1);
    assertThat(TextUtils.getIntValue(tree())).isNotPresent();
    assertThat(TextUtils.getIntValue(text("foo"))).isNotPresent();
    assertThat(TextUtils.getIntValue(null)).isNotPresent();
  }

  @Test
  void isValue() {
    assertThat(TextUtils.isValue(text("foo"), "foo")).isEqualTo(Trilean.TRUE);
    assertThat(TextUtils.isValue(text("foo"), "bar")).isEqualTo(Trilean.FALSE);
    assertThat(TextUtils.isValue(tree(), "foo")).isEqualTo(Trilean.UNKNOWN);
    assertThat(TextUtils.isValue(null, "foo")).isEqualTo(Trilean.UNKNOWN);
  }

  @Test
  void matchesValue() {
    assertThat(TextUtils.matchesValue(text("foo"), "foo"::equals)).isEqualTo(Trilean.TRUE);
    assertThat(TextUtils.matchesValue(text("foo"), "bar"::equals)).isEqualTo(Trilean.FALSE);
    assertThat(TextUtils.matchesValue(tree(), k -> k.startsWith("foo"))).isEqualTo(Trilean.UNKNOWN);
    assertThat(TextUtils.matchesValue(null, k -> k.startsWith("foo"))).isEqualTo(Trilean.UNKNOWN);
  }

  @Test
  void isValueTrue() {
    assertThat(TextUtils.isValueTrue(text("true"))).isTrue();
    assertThat(TextUtils.isValueTrue(text("false"))).isFalse();
    assertThat(TextUtils.isValueTrue(tree())).isFalse();
    assertThat(TextUtils.isValueTrue(null)).isFalse();
  }

  @Test
  void isValueFalse() {
    assertThat(TextUtils.isValueFalse(text("false"))).isTrue();
    assertThat(TextUtils.isValueFalse(text("true"))).isFalse();
    assertThat(TextUtils.isValueFalse(tree())).isFalse();
    assertThat(TextUtils.isValueFalse(null)).isFalse();
  }

  @ParameterizedTest
  @CsvSource(value = {
    "true, TRUE",
    "false, FALSE",
    "foobar, FALSE",
    "null, FALSE"
  }, nullValues = "null")
  void shouldConvertTextTreeToTrilean(String text, Trilean expected) {
    var trilean = TextUtils.trileanFromTextTree(text(text));
    assertThat(trilean).isEqualTo(expected);
  }
}
