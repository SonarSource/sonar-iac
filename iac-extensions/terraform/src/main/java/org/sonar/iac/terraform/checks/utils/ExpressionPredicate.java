/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.terraform.checks.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;

public class ExpressionPredicate {

  private static ExpressionPredicate instance;
  private final Map<String, Pattern> compiledPatterns = new HashMap<>();

  private ExpressionPredicate() {
  }

  /**
   * Use singleton pattern to make use of a compiled pattern lookup map
   */
  public static ExpressionPredicate getInstance() {
    if (instance == null) {
      instance = new ExpressionPredicate();
    }
    return instance;
  }

  private static Pattern pattern(String regex, int flags) {
    return getInstance().compiledPatterns.computeIfAbsent(String.format("pattern:%s,flags:%d", regex, flags), i -> Pattern.compile(regex, flags));
  }

  /**
   * Tests true iff the target expression is a string literal, and it's value is not equal to the expected one.
   */
  public static Predicate<ExpressionTree> notEqualTo(String expected) {
    return expression -> TextUtils.isValue(expression, expected).isFalse();
  }

  /**
   * Tests true iff the target expression is a string literal, and it's value is equal to the expected one.
   */
  public static Predicate<ExpressionTree> equalTo(String expected) {
    return expression -> TextUtils.isValue(expression, expected).isTrue();
  }

  /**
   * Tests true iff the target expression is a string literal that fully matches the pattern.
   */
  public static Predicate<ExpressionTree> matchesPattern(String pattern, int flags) {
    return expression -> TextUtils.matchesValue(expression, s -> pattern(pattern, flags).matcher(s).matches()).isTrue();
  }

  /**
   * Tests true iff the target expression is a string literal that fully matches the case-insensitive pattern.
   */
  public static Predicate<ExpressionTree> matchesPattern(String pattern) {
    return matchesPattern(pattern, Pattern.CASE_INSENSITIVE);
  }

  /**
   * Tests true iff the target expression is a string literal, and it's value is true.
   */
  public static Predicate<ExpressionTree> isTrue() {
    return TextUtils::isValueTrue;
  }

  /**
   * Tests true iff the target expression is a string literal, and it's value is false.
   */
  public static Predicate<ExpressionTree> isFalse() {
    return TextUtils::isValueFalse;
  }

  /**
   * Tests true iff the target expression is an int literal, and it's value is less than the provided.
   */
  public static Predicate<ExpressionTree> lessThan(int other) {
    return expression -> TextUtils.getIntValue(expression).filter(current -> current < other).isPresent();
  }
}
