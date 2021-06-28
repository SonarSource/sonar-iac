package org.sonar.iac.cloudformation.checks.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Stream;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
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
      // for now all paths have to start with slash
      throw new InvalidXPathExpression();
    }
    XPathUtils utils = new XPathUtils();
    StringTokenizer tokenizer = new StringTokenizer(expression, "/");
    if (tokenizer.countTokens() > 0) {
      utils.visitTree(root, tokenizer);
    } else {
      return Collections.singletonList(root);
    }
    return utils.result;
  }

  static boolean keyMatch(CloudformationTree key, String token) {
    return key instanceof ScalarTree && ((ScalarTree) key).value().equals(token);
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
        .filter(t -> keyMatch(t.key(), finalToken));

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

  }
}
