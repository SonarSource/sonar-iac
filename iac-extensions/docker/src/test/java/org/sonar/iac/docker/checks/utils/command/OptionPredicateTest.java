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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionPredicateTest {

  @Test
  void hasReturningTrueBothPredicatesDontMatch() {
    OptionPredicate optionPredicate = new OptionPredicate(SingularPredicate.equalMatch("flag"), new SingularPredicate("value"::equals, CommandPredicate.Type.NO_MATCH));

    assertTrue(optionPredicate.has(CommandPredicate.Type.NO_MATCH));
  }

  @Test
  void hasReturningTrueWhenValueNullAndFlagMatches() {
    OptionPredicate optionPredicate = new OptionPredicate(SingularPredicate.equalMatch("flag"));

    assertTrue(optionPredicate.has(CommandPredicate.Type.MATCH));
  }

  @Test
  void hasReturningFalseBothPredicatesDontMatch() {
    OptionPredicate optionPredicate = new OptionPredicate(SingularPredicate.equalMatch("flag"), new SingularPredicate("value"::equals, CommandPredicate.Type.NO_MATCH));

    assertFalse(optionPredicate.has(CommandPredicate.Type.OPTIONAL));
  }

  @Test
  void hasReturningFalseWhenValueNullAndDoesntMatch() {
    OptionPredicate optionPredicate = new OptionPredicate(SingularPredicate.equalMatch("flag"));

    assertFalse(optionPredicate.has(CommandPredicate.Type.OPTIONAL));
  }

}
