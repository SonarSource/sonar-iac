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
package org.sonar.iac.docker.checks.utils.command;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringQuotedSetPredicateTest {

  @Test
  void testSingleValue() {
    StringQuotedSetPredicate predicate = new StringQuotedSetPredicate("value");
    assertThat(predicate.test("value")).isTrue();
    assertThat(predicate.test("\"value\"")).isTrue();
    assertThat(predicate.test("'value'")).isTrue();

    assertThat(predicate.test(" value")).isFalse();
    assertThat(predicate.test("value ")).isFalse();
    assertThat(predicate.test(" value ")).isFalse();
    assertThat(predicate.test("other")).isFalse();
    assertThat(predicate.test("valueother")).isFalse();
    assertThat(predicate.test("value other")).isFalse();
    assertThat(predicate.test("\"value")).isFalse();
    assertThat(predicate.test("value\"")).isFalse();
    assertThat(predicate.test("'value")).isFalse();
    assertThat(predicate.test("value'")).isFalse();
  }

  @Test
  void testMultipleValue() {
    StringQuotedSetPredicate predicate = new StringQuotedSetPredicate(Set.of("string", "value"));
    assertThat(predicate.test("string")).isTrue();
    assertThat(predicate.test("\"string\"")).isTrue();
    assertThat(predicate.test("'string'")).isTrue();

    assertThat(predicate.test("value")).isTrue();
    assertThat(predicate.test("\"value\"")).isTrue();
    assertThat(predicate.test("'value'")).isTrue();

    assertThat(predicate.test(" other ")).isFalse();
  }
}
