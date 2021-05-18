/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.parser;

import com.sonar.sslr.api.typed.Optional;
import org.sonar.plugins.iac.terraform.api.tree.AttributeTree;
import org.sonar.plugins.iac.terraform.api.tree.BlockTree;
import org.sonar.plugins.iac.terraform.api.tree.BodyTree;
import org.sonar.plugins.iac.terraform.api.tree.ExpressionTree;
import org.sonar.plugins.iac.terraform.api.tree.FileTree;
import org.sonar.plugins.iac.terraform.api.tree.LabelTree;
import org.sonar.plugins.iac.terraform.api.tree.OneLineBlockTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.iac.terraform.tree.impl.AttributeTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.BlockTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.BodyTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.FileTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.LabelTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.LiteralExprTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.OneLineBlockTreeImpl;

import java.util.List;

public class TreeFactory {
  public FileTree file(Optional<BodyTree> body, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileTreeImpl(body.orNull(), eof);
  }

  public BodyTree body(List<Tree> statements) {
    return new BodyTreeImpl(statements);
  }

  public BlockTree block(SyntaxToken type, Optional<List<LabelTree>> labels, SyntaxToken openBrace, SyntaxToken newLine, Optional<BodyTree> body, SyntaxToken closeBrace) {
    return new BlockTreeImpl(type, labels.orNull(), openBrace, body.orNull(), closeBrace);
  }

  public OneLineBlockTree oneLineBlock(SyntaxToken type, Optional<List<LabelTree>> labels, SyntaxToken openBrace, Optional<AttributeTree> attribute, SyntaxToken closeBrace) {
    return new OneLineBlockTreeImpl(type, labels.orNull(), openBrace, attribute.orNull(), closeBrace);
  }

  public LabelTree label(SyntaxToken token) {
    return new LabelTreeImpl(token);
  }

  public LiteralExprTreeImpl literalExpr(SyntaxToken token) {
    return new LiteralExprTreeImpl(token);
  }

  public AttributeTree attribute(SyntaxToken name, SyntaxToken equalSign, ExpressionTree value) {
    return new AttributeTreeImpl(name, equalSign, value);
  }

}
