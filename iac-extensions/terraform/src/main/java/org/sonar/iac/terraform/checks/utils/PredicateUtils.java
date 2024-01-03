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
package org.sonar.iac.terraform.checks.utils;

import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public interface PredicateUtils {
  /** Given a regex string literal, compiles a regex Pattern, and creates a String Predicate
   * that will test a string 'true' only iff the regex pattern matches the whole string.
   */
  static Predicate<String> exactMatchStringPredicate(String regex) {
    return exactMatchStringPredicate(regex, 0);
  }

  /** Given a regex string literal and regex flags, compiles a regex Pattern, and creates a String Predicate
   * that will test a string 'true' only iff the regex pattern matches the whole string.
   */
  static Predicate<String> exactMatchStringPredicate(String regex, int flags) {
    final Pattern compiledPattern = Pattern.compile(regex, flags);
    return s -> compiledPattern.matcher(s).matches();
  }

  /** Given a regex string literal, compiles a regex pattern, and creates a string predicate
   * that will test a string 'true' only iff the regex pattern matches any substring of the given string.
   */
  static Predicate<String> containsMatchStringPredicate(String regex) {
    return containsMatchStringPredicate(regex, 0);
  }

  /** Given a regex string literal and regex flags, compiles a regex pattern, and creates a string predicate
   * that will test a string 'true' only iff the regex pattern matches any substring of the given string.
   */
  static Predicate<String> containsMatchStringPredicate(String regex, int flags) {
    final Pattern compiledPattern = Pattern.compile(regex, flags);
    return s -> compiledPattern.matcher(s).find();
  }

  /** Given a string predicate creates a tree predicate that tests a tree 'true'
   * only iff the string predicate tests tree's value 'true'
   */
  static <T extends Tree> Predicate<T> treePredicate(Predicate<String> stringPredicate) {
    return tree -> TextUtils.matchesValue(tree, stringPredicate).isTrue();
  }
}
