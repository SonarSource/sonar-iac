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
import java.util.function.Predicate;

public class StringQuotedSetPredicate implements Predicate<String> {
  private final Set<String> values;

  public StringQuotedSetPredicate(String value) {
    this.values = Set.of(value);
  }

  public StringQuotedSetPredicate(Set<String> values) {
    this.values = values;
  }

  @Override
  public boolean test(String s) {
    String text;
    if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
      text = s.substring(1, s.length() - 1);
    } else {
      text = s;
    }
    return values.contains(text);
  }
}
