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
package org.sonar.iac.terraform.tree.impl.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TupleTree;

/**
 * {@link TupleTree} backed by a JSON array. The list of element trees and the brace tokens are passed
 * in fully built; this class never constructs child nodes.
 */
public final class JsonTupleTreeImpl implements TupleTree {

  private final SeparatedTrees<ExpressionTree> elements;
  private final SyntaxToken openBrace;
  private final SyntaxToken closeBrace;
  private final TextRange range;

  public JsonTupleTreeImpl(SeparatedTrees<ExpressionTree> elements, SyntaxToken openBrace, SyntaxToken closeBrace, TextRange range) {
    this.elements = elements;
    this.openBrace = openBrace;
    this.closeBrace = closeBrace;
    this.range = range;
  }

  @Override
  public SeparatedTrees<ExpressionTree> elements() {
    return elements;
  }

  @Override
  public Iterator<ExpressionTree> iterator() {
    return elements.trees().iterator();
  }

  @Override
  public TextRange textRange() {
    return range;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openBrace);
    children.addAll(elements.treesAndSeparators());
    children.add(closeBrace);
    return children;
  }

  @Override
  public boolean is(Kind... kind) {
    for (Kind k : kind) {
      if (k == Kind.TUPLE) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Kind getKind() {
    return Kind.TUPLE;
  }
}
