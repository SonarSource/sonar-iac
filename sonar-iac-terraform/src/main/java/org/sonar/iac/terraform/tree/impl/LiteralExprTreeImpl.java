/*
 * SonarQube IaC Plugin
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
package org.sonar.iac.terraform.tree.impl;

import org.sonar.iac.terraform.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

import java.util.Collections;
import java.util.List;

public class LiteralExprTreeImpl extends TerraformTreeImpl implements LiteralExprTree {

  private final Kind kind;
  private final SyntaxToken token;

  public LiteralExprTreeImpl(Kind kind, SyntaxToken token) {
    this.kind = kind;
    this.token = token;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public SyntaxToken token() {
    return token;
  }

  @Override
  public String value() {
    if (is(Kind.STRING_LITERAL)) {
      return token.value().substring(1, token.value().length() - 1 );
    }
    return token.value();
  }

  @Override
  public List<Tree> children() {
    return Collections.singletonList(token);
  }
}
