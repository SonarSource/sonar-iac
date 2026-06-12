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

import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

/**
 * {@link ObjectElementTree} backed by a JSON object member. Key, separator and value are passed in
 * already built; this class never constructs child nodes.
 */
public final class JsonObjectElementTreeImpl implements ObjectElementTree {

  private final ExpressionTree key;
  private final SyntaxToken equalSign;
  private final ExpressionTree value;
  private final TextRange range;

  public JsonObjectElementTreeImpl(ExpressionTree key, SyntaxToken equalSign, ExpressionTree value, TextRange range) {
    this.key = key;
    this.equalSign = equalSign;
    this.value = value;
    this.range = range;
  }

  @Override
  public ExpressionTree key() {
    return key;
  }

  @Override
  public SyntaxToken equalOrColonSign() {
    return equalSign;
  }

  @Override
  public ExpressionTree value() {
    return value;
  }

  @Override
  public TextRange textRange() {
    return range;
  }

  @Override
  public List<Tree> children() {
    return List.of(key, equalSign, value);
  }

  @Override
  public boolean is(Kind... kind) {
    for (Kind k : kind) {
      if (k == Kind.OBJECT_ELEMENT) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT_ELEMENT;
  }
}
