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
package org.sonar.iac.common.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Stream;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

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
    if ("*".equals(token)) {
      var children = getChildValues(tree);
      if (!tokenizer.hasMoreTokens()) {
        result.addAll(children);
      } else {
        String nextToken = tokenizer.nextToken();
        children.forEach(e -> visitTree(e, tokenizer, nextToken));
      }
      return;
    }
    boolean expectSequence = false;
    if (token.endsWith("[]")) {
      expectSequence = true;
      token = token.substring(0, token.length() - 2);
    }

    if (tree instanceof MappingTree mappingTree) {
      String finalToken = token;
      Stream<TupleTree> tuples = mappingTree.elements().stream()
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

  private static List<YamlTree> getChildValues(YamlTree node) {
    if (node instanceof MappingTree mappingTree) {
      return mappingTree.elements()
        .stream()
        .map(TupleTree::value)
        .toList();
    }
    return node.children();
  }

  static class InvalidXPathExpression extends RuntimeException {
    public InvalidXPathExpression(String message) {
      super(message);
    }
  }
}
