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

public class OptionPredicate implements CommandPredicate {
  final SingularPredicate flagPredicate;

  final SingularPredicate valuePredicate;

  public OptionPredicate(SingularPredicate flagPredicate, SingularPredicate valuePredicate) {
    this.flagPredicate = flagPredicate;
    this.valuePredicate = valuePredicate;
  }

  public static OptionPredicate equalMatch(String expectedFlag, String expectedValue) {
    return new OptionPredicate(SingularPredicate.equalMatch(expectedFlag), SingularPredicate.equalMatch(expectedValue));
  }

  /**
   * true if either of the singularPredicates match
   */
  @Override
  public boolean has(Type... types) {
    return this.flagPredicate.has(types) || (this.valuePredicate != null && this.valuePredicate.has(types));
  }
}
