/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.springconfig.parser.properties;

import java.util.ArrayList;
import org.antlr.v4.runtime.ParserRuleContext;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.springconfig.tree.api.Profile;
import org.sonar.iac.springconfig.tree.api.Scalar;
import org.sonar.iac.springconfig.tree.api.SpringConfig;
import org.sonar.iac.springconfig.tree.api.Tuple;
import org.sonar.iac.springconfig.tree.impl.FileImpl;
import org.sonar.iac.springconfig.tree.impl.ProfileImpl;
import org.sonar.iac.springconfig.tree.impl.ScalarImpl;
import org.sonar.iac.springconfig.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.springconfig.tree.impl.TupleImpl;

import static org.sonar.iac.springconfig.parser.SpringConfigProfileNameUtil.profileName;

public class PropertiesParseTreeVisitor extends PropertiesParserBaseVisitor<SpringConfig> {

  @Override
  public SpringConfig visitPropertiesFile(PropertiesParser.PropertiesFileContext ctx) {
    super.visitPropertiesFile(ctx);
    var properties = new ArrayList<Tuple>();
    var comments = new ArrayList<Comment>();
    var profiles = new ArrayList<Profile>();
    for (PropertiesParser.RowContext row : ctx.row()) {
      if (row.line() != null) {
        var keyScalar = createKeyScalar(row);
        var valueScalar = createValueScalar(row);
        properties.add(new TupleImpl(keyScalar, valueScalar));
      }
      if (row.comment() != null) {
        var comment = createComment(row);
        if ("#---".equals(comment.value()) || "!---".equals(comment.value())) {
          var profile = new ProfileImpl(properties, comments, profileName(properties), true);
          profiles.add(profile);
          properties = new ArrayList<>();
          comments = new ArrayList<>();
        }
        comments.add(comment);
      }
    }
    var profile = new ProfileImpl(properties, comments, profileName(properties), true);
    profiles.add(profile);
    return new FileImpl(profiles);
  }

  private static Scalar createKeyScalar(PropertiesParser.RowContext row) {
    var keyContext = row.line().key().get(0);
    var keyText = keyContext.getText();
    return new ScalarImpl(new SyntaxTokenImpl(keyText, textRange(keyContext)));
  }

  private static Scalar createValueScalar(PropertiesParser.RowContext row) {
    Scalar valueScalar = null;
    if (row.line().key().size() == 2) {
      var valueContext = row.line().key().get(1);
      var valueText = valueContext.getText();
      valueScalar = new ScalarImpl(new SyntaxTokenImpl(valueText, textRange(valueContext)));
    }
    return valueScalar;
  }

  private static Comment createComment(PropertiesParser.RowContext row) {
    var commentContext = row.comment().commentStartAndText();
    var value = commentContext.getText();
    var contentText = commentContext.commentText().getText();
    return new CommentImpl(value, contentText, textRange(commentContext));
  }

  private static TextRange textRange(ParserRuleContext ctx) {
    return TextRanges.range(
      ctx.start.getLine(),
      ctx.start.getCharPositionInLine(),
      ctx.stop.getLine(),
      // ANTLR treats the end as the position of the last character, while for us it should be the next index.
      ctx.stop.getCharPositionInLine() + 1);
  }
}
