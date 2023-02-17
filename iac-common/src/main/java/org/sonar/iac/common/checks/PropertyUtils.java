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
package org.sonar.iac.common.checks;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;

public class PropertyUtils {

  private PropertyUtils() {
    // Utils class
  }

  public static Trilean has(@Nullable Tree tree, String key) {
    if (tree instanceof HasProperties) {
      Set<Trilean> elementTrileans = ((HasProperties) tree).properties().stream()
        .map(element -> TextUtils.isValue(element.key(), key))
        .collect(Collectors.toSet());
      if (elementTrileans.contains(Trilean.TRUE)) return Trilean.TRUE;
      if (elementTrileans.contains(Trilean.UNKNOWN)) return Trilean.UNKNOWN;
    }
    return Trilean.FALSE;
  }

  public static boolean valueIs(@Nullable Tree tree, String key, Predicate<Tree> predicate) {
    return value(tree, key).filter(predicate).isPresent();
  }

  // Check whether a particular property can be unambiguously considered absent.
  public static boolean isMissing(@Nullable Tree tree, String key) {
    return has(tree, key).isFalse();
  }

  public static Optional<PropertyTree> get(@Nullable Tree tree, String key) {
    return get(tree, key::equals);
  }

  public static List<PropertyTree> getAll(@Nullable Tree tree, String key) {
    return getAll(tree, key::equals).collect(Collectors.toList());
  }

  public static <T extends Tree> List<T> getAll(@Nullable Tree tree, String key, Class<T> clazz) {
    return getAll(tree, key::equals).filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
  }

  public static <T extends Tree> List<T> getAll(@Nullable Tree tree, Class<T> clazz) {
    return getAll(tree, t -> true).filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
  }

  private static Optional<PropertyTree> get(@Nullable Tree tree, Predicate<String> keyMatcher) {
    return getAll(tree, keyMatcher).findFirst();
  }

  private static Stream<PropertyTree> getAll(@Nullable Tree tree, Predicate<String> keyMatcher) {
    if (!(tree instanceof HasProperties)) return Stream.empty();
    return ((HasProperties) tree).properties().stream()
      .filter(attribute -> TextUtils.matchesValue(attribute.key(), keyMatcher).isTrue());
  }

  public static <T extends Tree> Optional<T> get(@Nullable Tree tree, String key, Class<T> clazz) {
    return get(tree, key).filter(clazz::isInstance).map(clazz::cast);
  }

  public static Optional<Tree> key(@Nullable Tree tree, String key) {
    return get(tree, key).map(PropertyTree::key);
  }

  public static Optional<Tree> value(@Nullable Tree tree, String key) {
    return get(tree, key).map(PropertyTree::value);
  }

  public static Optional<Tree> value(@Nullable Tree tree, Predicate<String> keyMatcher) {
    return get(tree, keyMatcher).map(PropertyTree::value);
  }

  public static <T extends Tree> Optional<T> value(@Nullable Tree tree, String key, Class<T> clazz) {
    return value(tree, key)
      .filter(clazz::isInstance)
      .map(clazz::cast);
  }

  @CheckForNull
  public static Tree valueOrNull(@Nullable Tree tree, String key) {
    return valueOrNull(tree, key, Tree.class);
  }

  @CheckForNull
  public static <T extends Tree> T valueOrNull(@Nullable Tree tree, String key, Class<T> clazz) {
    return value(tree, key, clazz).orElse(null);
  }
}
