/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.checks;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

  public static Optional<PropertyTree> get(@Nullable Tree tree, String key) {
    return get(tree, key::equals);
  }

  private static Optional<PropertyTree> get(@Nullable Tree tree, Predicate<String> keyMatcher) {
    if (!(tree instanceof HasProperties)) return Optional.empty();
    return ((HasProperties) tree).properties().stream()
      .filter(attribute -> TextUtils.matchesValue(attribute.key(), keyMatcher).isTrue())
      .findFirst();
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
