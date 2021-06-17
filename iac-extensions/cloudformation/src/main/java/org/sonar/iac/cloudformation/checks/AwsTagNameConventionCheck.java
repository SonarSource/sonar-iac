/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;
import java.util.regex.Pattern;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.AbstractAwsTagNameConventionCheck;

public class AwsTagNameConventionCheck extends AbstractAwsTagNameConventionCheck {

  @Override
  public void initialize(InitContext init) {
    pattern = Pattern.compile(format);
    init.register(TupleTree.class, (ctx, tree) -> {
      if (!(tree.key() instanceof ScalarTree && "tags".equalsIgnoreCase(((ScalarTree)tree.key()).value()) && tree.value() instanceof SequenceTree)) {
        return;
      }

      check(ctx, (SequenceTree) tree.value());
    });
  }

  private void check(CheckContext ctx, SequenceTree tree) {
    tree.elements().stream()
      .map(AwsTagNameConventionCheck::tagKey)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .forEach(key -> {
        String value = key.value();
        if (!pattern.matcher(value).matches()) {
          ctx.reportIssue(key, String.format(MESSAGE, value, format));
        }
      });
  }

  private static Optional<ScalarTree> tagKey(CloudformationTree tree) {
    if (!(tree instanceof MappingTree)) {
      return Optional.empty();
    }

    ScalarTree tagKey = null;
    for (TupleTree element: ((MappingTree) tree).elements()) {
      if (element.key() instanceof ScalarTree && "key".equalsIgnoreCase(((ScalarTree)element.key()).value()) && isQuotedStringScalar(element.value())) {
        tagKey = (ScalarTree)element.value();
        break;
      }
    }

    return Optional.ofNullable(tagKey);
  }

  private static boolean isQuotedStringScalar(CloudformationTree tree) {
    return tree instanceof ScalarTree &&
      (((ScalarTree)tree).style().equals(ScalarTree.Style.DOUBLE_QUOTED) || ((ScalarTree)tree).style().equals(ScalarTree.Style.SINGLE_QUOTED));
  }
}
