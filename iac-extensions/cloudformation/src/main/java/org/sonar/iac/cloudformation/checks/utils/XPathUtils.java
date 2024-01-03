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
package org.sonar.iac.cloudformation.checks.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Stream;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.checks.TextUtils;

public class XPathUtils {

  private final List<YamlTree> result = new ArrayList<>();

  private XPathUtils() {
  }

  public static Optional<YamlTree> getSingleTree(YamlTree root, String expression) {
    List<YamlTree> trees = getTrees(root, expression);
    if (trees.size() == 1) {
      return Optional.of(trees.get(0));
    }
    return Optional.empty();
  }

  public static List<YamlTree> getTrees(YamlTree root, String expression) {
    if (!expression.startsWith("/")) {
      throw new InvalidXPathExpression("For now all paths have to start with slash");
    }
    XPathUtils utils = new XPathUtils();
    utils.visitTree(root, new StringTokenizer(expression, "/"));
    return utils.result;
  }

  void visitTree(YamlTree tree, StringTokenizer tokenizer) {
    if (!tokenizer.hasMoreTokens()) {
      result.add(tree);
      return;
    }
    visitTree(tree, tokenizer, tokenizer.nextToken());
  }

  void visitTree(YamlTree tree, StringTokenizer tokenizer, String token) {
    boolean expectSequence = false;
    if (token.endsWith("[]")) {
      expectSequence = true;
      token = token.substring(0, token.length() - 2);
    }

    if (tree instanceof MappingTree) {
      String finalToken = token;
      Stream<TupleTree> tuples = ((MappingTree) tree).elements().stream()
        .filter(t -> TextUtils.isValue(t.key(), finalToken).isTrue());

      if (expectSequence) {
        tuples
          .map(TupleTree::value)
          .filter(SequenceTree.class::isInstance)
          .forEach(t -> {
            if (!tokenizer.hasMoreTokens()) {
              result.addAll(((SequenceTree) t).elements());
            } else {
              String nextToken = tokenizer.nextToken();
              ((SequenceTree) t).elements().forEach(e -> visitTree(e, tokenizer, nextToken));
            }
          });
      } else {
        tuples.forEach(t -> visitTree(t.value(), tokenizer));
      }
    }
  }

  static class InvalidXPathExpression extends RuntimeException {
    public InvalidXPathExpression(String message) {
      super(message);
    }
  }
}
