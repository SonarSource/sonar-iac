/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

import javax.annotation.Nullable;

public class TupleTreeImpl extends AbstractCollectionValueTree<ExpressionTree> implements TupleTree {

  public TupleTreeImpl(SyntaxToken openBrace, @Nullable SeparatedTrees<ExpressionTree> elements, SyntaxToken closeBrace) {
    super(openBrace, elements, closeBrace);
  }

  @Override
  public Kind getKind() {
    return Kind.TUPLE;
  }
}
