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

import java.util.function.Predicate;

public class SingularPredicate implements CommandPredicate {
  final Predicate<String> predicate;
  final Type type;

  public SingularPredicate(Predicate<String> predicate, Type type) {
    this.predicate = predicate;
    this.type = type;
  }

  public static SingularPredicate equalMatch(String string) {
    return new SingularPredicate(string::equals, Type.MATCH);
  }

  public boolean is(Type... types) {
    for (Type t : types) {
      if (this.type.equals(t)) {
        return true;
      }
    }
    return false;
  }
}
