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

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MultipleUnorderedOptionsPredicateTest {

  @Test
  void hasReturningFalse() {
    List<OptionPredicate> options = List.of(OptionPredicate.equalMatch("-flag", "value"));
    MultipleUnorderedOptionsPredicate multipleOptionsPredicate = new MultipleUnorderedOptionsPredicate(options);

    assertFalse(multipleOptionsPredicate.hasType(CommandPredicate.Type.NO_MATCH));
  }

  @Test
  void hasReturningFalseOnEmptyList() {
    MultipleUnorderedOptionsPredicate multipleOptionsPredicate = new MultipleUnorderedOptionsPredicate(Collections.emptyList());

    assertFalse(multipleOptionsPredicate.hasType(CommandPredicate.Type.NO_MATCH));
  }

  @Test
  void anyMatchPredicateValueShouldNotMatchOnFlag() {
    List<OptionPredicate> options = List.of(OptionPredicate.equalMatch("-flag", "value"));
    MultipleUnorderedOptionsPredicate multipleOptionsPredicate = new MultipleUnorderedOptionsPredicate(options);

    OptionPredicate anyOptionPredicate = multipleOptionsPredicate.calculateAnyOptionMatchingExceptExpected();

    assertFalse(anyOptionPredicate.valuePredicate.predicate.test("-random"));
  }

  @Test
  void anyMatchPredicateValueShouldNotMatchOnNewCommand() {
    List<OptionPredicate> options = List.of(OptionPredicate.equalMatch("-flag", "value"));
    MultipleUnorderedOptionsPredicate multipleOptionsPredicate = new MultipleUnorderedOptionsPredicate(options);

    OptionPredicate anyOptionPredicate = multipleOptionsPredicate.calculateAnyOptionMatchingExceptExpected();

    assertFalse(anyOptionPredicate.valuePredicate.predicate.test("&&"));
  }
}
