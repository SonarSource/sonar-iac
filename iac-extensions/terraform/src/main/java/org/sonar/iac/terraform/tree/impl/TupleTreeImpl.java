/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TupleTree;

public class TupleTreeImpl extends AbstractCollectionValueTree<ExpressionTree> implements TupleTree {

  public TupleTreeImpl(SyntaxToken openBrace, @Nullable SeparatedTrees<ExpressionTree> elements, SyntaxToken closeBrace) {
    super(openBrace, elements, closeBrace);
  }

  @Override
  public Kind getKind() {
    return Kind.TUPLE;
  }

  @Override
  public Iterator<ExpressionTree> iterator() {
    return elements().trees().iterator();
  }
}
