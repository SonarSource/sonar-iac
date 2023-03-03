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
import java.util.function.Predicate;
import javax.print.Doc;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.DockerTree;

public class TreeUtils {

  private TreeUtils() {}

  public static Optional<Tree> getParent(Tree tree, Predicate<Tree> predicate) {
    do {
      tree = ((DockerTree) tree).parent();
    } while(tree != null && !predicate.test(tree));
    return Optional.ofNullable(tree);
  }

  public static Optional<Tree> getLastDescendant(Tree tree, Predicate<Tree> predicate) {
    Tree last = null;
    for (Tree child : tree.children()) {
      DockerTree dockerChild = (DockerTree) child;
      if (predicate.test(dockerChild)) {
        last = dockerChild;
      }
      Optional<Tree> result = getLastDescendant(dockerChild, predicate);
      if (result.isPresent()) {
        last = result.get();
      }
    }
    return Optional.ofNullable(last);
  }
}
