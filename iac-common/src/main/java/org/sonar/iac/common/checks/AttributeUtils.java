/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.checks;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.AttributeTree;
import org.sonar.iac.common.api.tree.Tree;

public class AttributeUtils {

  private AttributeUtils() {
    // Utils class
  }

  public static Trilean has(@Nullable Tree tree, String key) {
    if (tree != null) {
      List<Trilean> elementTrileans = tree.attributes().stream().map(element -> TextUtils.isValue(element.key(), key)).collect(Collectors.toList());
      if (elementTrileans.stream().anyMatch(Trilean::isTrue)) return Trilean.TRUE;
      if (elementTrileans.stream().anyMatch(Trilean::isUnknown)) return Trilean.UNKNOWN;
    }
    return Trilean.FALSE;
  }

  public static <T extends AttributeTree> Optional<T> get(@Nullable Tree tree, String key) {
    if (tree == null) return Optional.empty();
    try {
      return tree.attributes().stream()
        .filter(attribute -> TextUtils.isValue(attribute.key(), key).isTrue())
        .map(attribute -> (T) attribute)
        .findFirst();
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  public static <T extends Tree> Optional<T> key(@Nullable Tree tree, String key) {
    try {
      return get(tree, key).map(attribute -> (T) attribute.key());
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  public static <T extends Tree> Optional<T> value(@Nullable Tree tree, String key) {
    try {
      return get(tree, key).map(attribute -> (T) attribute.value());
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  public static <T extends Tree> T valueOrNull(@Nullable Tree tree, String key) {
    try {
      return get(tree, key).map(attribute -> (T) attribute.value()).orElse(null);
    } catch (RuntimeException e) {
      return null;
    }
  }
}
