/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Stream;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;

public class XPathUtils {

  private final List<CloudformationTree> result = new ArrayList<>();

  private XPathUtils() {
  }

  public static Optional<CloudformationTree> getSingleTree(CloudformationTree root, String expression) {
    List<CloudformationTree> trees = getTrees(root, expression);
    if (trees.size() == 1) {
      return Optional.of(trees.get(0));
    }
    return Optional.empty();
  }

  public static List<CloudformationTree> getTrees(CloudformationTree root, String expression) {
    if (!expression.startsWith("/")) {
      throw new InvalidXPathExpression("For now all paths have to start with slash");
    }
    XPathUtils utils = new XPathUtils();
    utils.visitTree(root, new StringTokenizer(expression, "/"));
    return utils.result;
  }

  void visitTree(CloudformationTree tree, StringTokenizer tokenizer) {
    if (!tokenizer.hasMoreTokens()) {
      result.add(tree);
      return;
    }
    visitTree(tree, tokenizer, tokenizer.nextToken());
  }

  void visitTree(CloudformationTree tree, StringTokenizer tokenizer, String token) {
    boolean expectSequence = false;
    if (token.endsWith("[]")) {
      expectSequence = true;
      token = token.substring(0, token.length() - 2);
    }

    if (tree instanceof MappingTree) {
      String finalToken = token;
      Stream<TupleTree> tuples = ((MappingTree) tree).elements().stream()
        .filter(t -> ScalarTreeUtils.isValue(t.key(), finalToken));

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
