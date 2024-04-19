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
package org.sonar.iac.arm.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;

public class ArmTreeUtils {

  public static final String ARRAY_TOKEN = "*";

  private ArmTreeUtils() {
    // utils class to manipulate ArmTree components
  }

  public static List<String> computePath(String path) {
    return Arrays.asList(path.split("/"));
  }

  public static List<Tree> resolveProperties(String path, Tree tree) {
    Queue<String> pathElements = new LinkedList<>(Arrays.asList(path.split("/")));
    return resolveProperties(pathElements, tree);
  }

  /**
   * This method is used to retrieve the list of tree elements which can be resolved using a provided queue of path.
   * The result is a list of Tree because it can resolve to multiple element since it is supporting the token '*' to means that an array is expected.
   * Example:
   *   Provided list of path: "connections", "*", "entry"
   *   Provided Tree representation:
   *   {
   *     "connections": [
   *       { "entry":"val1" }, --> will be in the result list
   *       { "entry":"val2" }  --> will also be in the result list
   *     ]
   *   }
   */
  public static List<Tree> resolveProperties(Queue<String> path, Tree tree) {
    while (!path.isEmpty() && tree != null) {
      String nextPath = path.poll();
      if (nextPath.equals(ARRAY_TOKEN)) {
        if (tree instanceof ArrayExpression array) {
          List<Tree> trees = new ArrayList<>();
          array.elements().forEach(element -> trees.addAll(resolveProperties(new LinkedList<>(path), element)));
          return trees;
        } else {
          return Collections.emptyList();
        }
      } else {
        tree = PropertyUtils.value(tree, nextPath).orElse(null);
      }
    }
    return tree != null ? List.of(tree) : Collections.emptyList();
  }

  public static ArmTree getRootNode(ArmTree tree) {
    while (true) {
      ArmTree parent = tree.parent();
      if (parent == null) {
        return tree;
      } else {
        tree = parent;
      }
    }
  }

  public static Map<String, ParameterDeclaration> getParametersByNames(ArmTree tree) {
    // TODO: after SONARIAC-1034 use symbol table instead of accessing parameters through `FILE`
    File file = (File) ArmTreeUtils.getRootNode(tree);
    return file.statements().stream()
      .filter(ParameterDeclaration.class::isInstance)
      .map(ParameterDeclaration.class::cast)
      .collect(Collectors.toMap(param -> param.declaratedName().value(), param -> param));
  }

  public static Predicate<Expression> containsParameterReference(Collection<String> parameterNames) {
    return expr -> expr instanceof HasIdentifier hasIdentifier &&
      hasIdentifier.identifier() instanceof Identifier identifier &&
      parameterNames.contains(identifier.value());
  }

  public static Expression retrieveIdentifierOrExpression(Expression expr) {
    if (expr instanceof HasIdentifier hasIdentifier) {
      return hasIdentifier.identifier();
    } else {
      return expr;
    }
  }

  public static <T extends ArmTree> Stream<T> findAllNodes(ArmTree tree, Class<T> type) {
    Stream<T> result = Stream.empty();
    if (type.isInstance(tree)) {
      result = Stream.of(type.cast(tree));
    }
    return Stream.concat(result, tree.children().stream()
      .flatMap(child -> findAllNodes((ArmTree) child, type))
      .filter(type::isInstance)
      .map(type::cast));
  }
}
