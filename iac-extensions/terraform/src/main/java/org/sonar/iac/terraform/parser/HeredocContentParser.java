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
package org.sonar.iac.terraform.parser;

import com.eclipsesource.json.JsonParser;
import com.eclipsesource.json.ParseException;
import java.util.Collections;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.tree.impl.LiteralExprTreeImpl;
import org.sonar.iac.terraform.tree.impl.SyntaxTokenImpl;

/**
 * Parses the body of a Terraform heredoc literal and builds an {@link ExpressionTree} representation:
 *
 * <ul>
 *   <li>If the body is a valid JSON object — returns a {@code ObjectTree} (and recursively built children)
 *       structurally equivalent to what {@code jsonencode({...})} would produce. Each node carries a
 *       precise {@link TextRange} pointing back at the corresponding source span inside the heredoc.</li>
 *   <li>Otherwise (invalid JSON, non-object JSON root, free-form text) — returns a {@code LiteralExprTree}
 *       wrapping the raw body without the surrounding {@code <<TAG ... TAG} markers. The literal's range
 *       falls back to the entire heredoc literal's range, since there's no inner structure to attribute.</li>
 * </ul>
 *
 * <p>For JSON bodies the actual tree construction lives in {@link JsonBodyHandler}, a {@code minimal-json}
 * {@link com.eclipsesource.json.JsonHandler} that builds nodes incrementally with positions taken from the
 * parser's {@link com.eclipsesource.json.Location}.
 */
public final class HeredocContentParser {

  private HeredocContentParser() {
  }

  /**
   * Parse the content of the given heredoc literal token. Per-node {@link TextRange} values are derived
   * from the parser's body-local {@code Location} translated to file coordinates by offsetting line numbers
   * with the line of the surrounding {@code <<TAG} marker (the heredoc body sits on the line immediately
   * after).
   *
   * <p>Currently only JSON object bodies are interpreted structurally. Any other body is returned as a
   * plain {@link LiteralExprTree} wrapping the raw body via general (non-JSON) tree node types.
   * Additional content kinds can be added to this method in the future without changing the shape of the result.
   */
  public static ExpressionTree parse(SyntaxToken heredocToken) {
    TextRange range = heredocToken.textRange();
    String body = stripMarkers(heredocToken.value());
    if (body != null) {
      try {
        JsonBodyHandler handler = new JsonBodyHandler(range.start().line());
        new JsonParser(handler).parse(body);
        ExpressionTree parsed = handler.getResult();
        if (parsed instanceof ObjectTree) {
          return parsed;
        }
      } catch (ParseException | UnsupportedOperationException e) {
        // Body is not valid JSON — fall through to the plain-text wrapper below.
      }
    }
    String raw = (body != null) ? body : heredocToken.value();
    return plainTextLiteral(raw, range);
  }

  /**
   * Strip the {@code <<TAG\n} / {@code <<-TAG\n} prefix and the closing {@code \nTAG} suffix from a heredoc
   * literal text. Returns {@code null} if the literal does not match the expected heredoc shape.
   */
  static String stripMarkers(String text) {
    if (!text.startsWith("<<")) {
      return null;
    }
    int newline = text.indexOf('\n');
    if (newline < 0) {
      return null;
    }
    int closingNewline = text.lastIndexOf('\n');
    if (closingNewline <= newline) {
      return null;
    }
    return text.substring(newline + 1, closingNewline);
  }

  /**
   * Plain-text fallback — built from the general Terraform node types ({@link SyntaxTokenImpl} and
   * {@link LiteralExprTreeImpl}). Used when the heredoc body is not interpretable as JSON.
   */
  private static LiteralExprTree plainTextLiteral(String value, TextRange range) {
    SyntaxToken token = new SyntaxTokenImpl(value, range, Collections.emptyList());
    return new LiteralExprTreeImpl(Kind.TEMPLATE_STRING_PART_LITERAL, token);
  }
}
