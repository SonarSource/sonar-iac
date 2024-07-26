/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public final class StringPredicate {
  private StringPredicate() {
  }

  public static Predicate<String> startsWithIgnoreQuotes(String value) {
    return str -> {
      System.out.println("AAA startsWithIgnoreQuotes " + value + " result " + stripQuotes(str).startsWith(value));
      return stripQuotes(str).startsWith(value);
    };
  }

  public static Predicate<String> startsWithIgnoreQuotes(List<String> values) {
    return str -> {
      for (String value : values) {
        if (stripQuotes(str).startsWith(value)) {
          return true;
        }
      }
      return false;
    };
  }

  public static Predicate<String> equalsIgnoreQuotes(String value) {
    return str -> stripQuotes(str).equals(value);
  }

  public static Predicate<String> containsIgnoreQuotes(Collection<String> values) {
    return str -> values.contains(stripQuotes(str));
  }

  static String stripQuotes(String s) {
    if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
      return s.substring(1, s.length() - 1);
    } else {
      return s;
    }
  }
}
