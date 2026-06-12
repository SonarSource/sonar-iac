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

import java.util.Collections;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

/**
 * {@link LiteralExprTree} backed by a JSON-derived scalar. Carries the exact {@link Kind} of the JSON
 * value ({@link Kind#STRING_LITERAL}, {@link Kind#BOOLEAN_LITERAL}, {@link Kind#NUMERIC_LITERAL} or
 * {@link Kind#NULL_LITERAL}) so checks gating on {@code is(Kind.BOOLEAN_LITERAL)} etc. behave the same
 * way against heredoc-embedded JSON as against native HCL values.
 *
 * <p>Unlike {@link org.sonar.iac.terraform.tree.impl.LiteralExprTreeImpl} the value stored here is
 * already unquoted (JSON scalars don't carry their delimiters into the wrapper).
 */
public final class JsonLiteralExprTreeImpl implements LiteralExprTree {

  private final Kind kind;
  private final String value;
  private final SyntaxToken token;
  private final TextRange range;

  public JsonLiteralExprTreeImpl(Kind kind, String value, SyntaxToken token, TextRange range) {
    this.kind = kind;
    this.value = value;
    this.token = token;
    this.range = range;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public SyntaxToken token() {
    return token;
  }

  @Override
  public TextRange textRange() {
    return range;
  }

  @Override
  public List<Tree> children() {
    return Collections.singletonList(token);
  }

  @Override
  public boolean is(Kind... kinds) {
    for (Kind k : kinds) {
      if (k == kind) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Kind getKind() {
    return kind;
  }
}
