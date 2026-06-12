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
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

/**
 * Synthetic syntax token used by JSON-derived Terraform tree nodes. Each token carries a
 * {@link TextRange} computed from the minimal-json {@link com.eclipsesource.json.Location} by
 * {@link org.sonar.iac.terraform.parser.JsonBodyHandler}: scalar values and structural delimiters
 * receive precise per-character ranges, while synthetic comma separators (which have no precise
 * position in the handler API) fall back to the surrounding container range.
 */
public final class JsonSyntaxTokenImpl implements SyntaxToken {

  private final String value;
  private final TextRange range;

  public JsonSyntaxTokenImpl(String value, TextRange range) {
    this.value = value;
    this.range = range;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public List<Comment> comments() {
    return Collections.emptyList();
  }

  @Override
  public TextRange textRange() {
    return range;
  }

  @Override
  public List<Tree> children() {
    return Collections.emptyList();
  }

  @Override
  public boolean is(Kind... kind) {
    for (Kind k : kind) {
      if (k == Kind.TOKEN) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Kind getKind() {
    return Kind.TOKEN;
  }
}
