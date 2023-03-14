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
package org.sonar.iac.docker.tree;

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;

public class TreeUtils {

  private TreeUtils() {}

  public static <T extends Tree> Optional<T> firstDescendant(Tree root, Class<T> clazz) {
    return (Optional<T>) firstDescendant(root, clazz::isInstance);
  }

  public static Optional<Tree> firstDescendant(@Nullable Tree root, Predicate<Tree> predicate) {
    return descendants(root).filter(predicate).findFirst();
  }

  public static Optional<Tree> lastDescendant(@Nullable Tree root, Predicate<Tree> predicate) {
    return descendants(root).filter(predicate).reduce((first, second) -> second);
  }

  private static Stream<Tree> descendants(@Nullable Tree root) {
    if (root == null || root.children().isEmpty()) {
      return Stream.empty();
    }
    Spliterator<Tree> spliterator = Spliterators.spliteratorUnknownSize(root.children().iterator(), Spliterator.ORDERED);
    Stream<Tree> stream = StreamSupport.stream(spliterator, false);
    return stream.flatMap(tree -> Stream.concat(Stream.of(tree), descendants(tree)));
  }
}
