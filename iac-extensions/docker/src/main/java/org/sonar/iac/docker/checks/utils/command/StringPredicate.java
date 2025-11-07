/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.Collection;
import java.util.function.Predicate;

public final class StringPredicate {
  private StringPredicate() {
  }

  public static Predicate<String> startsWithIgnoreQuotes(String value) {
    return str -> stripQuotes(str).startsWith(value);
  }

  public static Predicate<String> startsWithIgnoreQuotes(String... values) {
    return (String str) -> {
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
