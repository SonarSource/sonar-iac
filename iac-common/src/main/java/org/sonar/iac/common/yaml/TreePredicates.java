/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.common.yaml;

import java.util.List;
import java.util.function.Predicate;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.YamlTree;

public final class TreePredicates {

  private static final List<String> STRINGS_CONSIDERED_AS_EMPTY = List.of("", "~", "[]", "null");

  private TreePredicates() {
  }

  public static Predicate<YamlTree> isTrue() {
    return TextUtils::isValueTrue;
  }

  public static Predicate<YamlTree> isEqualTo(String parameter) {
    return t -> TextUtils.isValue(t, parameter).isTrue();
  }

  public static Predicate<YamlTree> isSet() {
    return t -> TextUtils.matchesValue(t, isSetString()).isTrue();
  }

  public static Predicate<String> isSetString() {
    return value -> !STRINGS_CONSIDERED_AS_EMPTY.contains(value);
  }

  public static Predicate<YamlTree> startsWith(List<String> strings) {
    return t -> strings.stream()
      .anyMatch(string -> TextUtils.matchesValue(t, value -> value.startsWith(string)).isTrue());
  }
}
