/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.common.yaml;

import java.util.List;
import java.util.function.Predicate;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class TreePredicates {

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
    return t -> TextUtils.matchesValue(t, value -> !STRINGS_CONSIDERED_AS_EMPTY.contains(value)).isTrue();
  }
}
