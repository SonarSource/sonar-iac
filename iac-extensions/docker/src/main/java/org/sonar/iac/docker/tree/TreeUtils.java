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
package org.sonar.iac.docker.tree;

import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.Instruction;

public final class TreeUtils {

  private TreeUtils() {
  }

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

  public static Optional<DockerTree> firstAncestor(DockerTree node, Predicate<DockerTree> predicate) {
    var parent = node.parent();
    if (parent == null) {
      return Optional.empty();
    } else if (predicate.test(parent)) {
      return Optional.of(parent);
    } else {
      return firstAncestor(parent, predicate);
    }
  }

  public static Optional<DockerTree> firstAncestorOfKind(DockerTree node, DockerTree.Kind... kinds) {
    return firstAncestor(node, tree -> Stream.of(kinds).anyMatch(tree::is));
  }

  /**
   * Refer to this value in the FROM instruction.
   * <pre>
   * {@code
   * FROM scratch AS my_image
   *      ^^^^^^^
   * }
   * </pre>
   */
  public static Optional<String> getParentDockerImageName(Instruction instruction) {
    return firstAncestorOfKind(instruction, DockerTree.Kind.DOCKERIMAGE)
      .map(DockerImage.class::cast)
      .map(DockerImage::from)
      .map(from -> ArgumentResolution.of(from.image()))
      .filter(ArgumentResolution::isResolved)
      .map(ArgumentResolution::value);
  }

  /**
   * Refer to this value in the FROM instruction.
   * <pre>
   * {@code
   * FROM scratch AS my_image
   *                 ^^^^^^^^
   * }
   * </pre>
   */
  public static Optional<String> getDockerImageName(Instruction instruction) {
    return firstAncestorOfKind(instruction, DockerTree.Kind.DOCKERIMAGE)
      .map(DockerImage.class::cast)
      .map(DockerImage::from)
      .map(FromInstruction.class::cast)
      .map(FromInstruction::alias)
      .filter(Objects::nonNull)
      .map(alias -> alias.alias().value());
  }
}
