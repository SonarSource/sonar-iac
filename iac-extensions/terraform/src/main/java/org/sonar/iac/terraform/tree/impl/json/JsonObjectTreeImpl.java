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
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

/**
 * {@link ObjectTree} backed by a JSON object. The list of element trees and the brace tokens are passed
 * in fully built; this class never constructs child nodes.
 */
public final class JsonObjectTreeImpl implements ObjectTree {

  private final SeparatedTrees<ObjectElementTree> elements;
  private final List<ObjectElementTree> properties;
  private final SyntaxToken openBrace;
  private final SyntaxToken closeBrace;
  private final TextRange range;

  public JsonObjectTreeImpl(SeparatedTrees<ObjectElementTree> elements, SyntaxToken openBrace, SyntaxToken closeBrace, TextRange range) {
    this.elements = elements;
    this.properties = elements.trees();
    this.openBrace = openBrace;
    this.closeBrace = closeBrace;
    this.range = range;
  }

  @Override
  public SeparatedTrees<ObjectElementTree> elements() {
    return elements;
  }

  @Override
  public List<ObjectElementTree> properties() {
    return properties;
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
      if (k == Kind.OBJECT) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT;
  }
}
