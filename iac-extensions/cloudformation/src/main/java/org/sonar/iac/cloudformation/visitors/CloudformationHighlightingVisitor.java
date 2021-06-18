/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.visitors;

import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

public class CloudformationHighlightingVisitor extends SyntaxHighlightingVisitor {

  @Override
  protected void languageSpecificHighlighting() {
    register(TupleTree.class, (ctx, tree) -> highlight(tree.key(), KEYWORD));
    register(ScalarTree.class, (ctx, tree) -> ctx.ancestors().stream().findFirst().ifPresent(p -> {
      if (!(p instanceof TupleTree && ((TupleTree) p).key().equals(tree))) {
        highlight(tree, STRING);
      }
    }));
  }

}
