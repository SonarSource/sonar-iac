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

import org.sonar.iac.arm.tree.api.*;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArmTreeUtils {

  public static final String ARRAY_TOKEN = "*";
  private static final Pattern jsonParameters = Pattern.compile("^\\[\\s*+parameters\\('(?<name>.*)'\\)\\s*+\\]$");

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
    return expr -> {
      // TODO SONARIAC-1038 ARM Json: parse expression in string and build the AST to be same as Bicep equivalent
      if (expr.is(ArmTree.Kind.IDENTIFIER)) {
        // ARM Bicep
        return parameterNames.contains(((Identifier) expr).value());
      } else if (expr.is(ArmTree.Kind.STRING_LITERAL)) {
        // ARM Json
        Matcher matcher = jsonParameters.matcher(((StringLiteral) expr).value());
        if (matcher.find()) {
          return parameterNames.contains(matcher.group("name"));
        }
      }
      return false;
    };
  }
}
