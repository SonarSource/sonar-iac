/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.tree.impl;

import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Alias;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class AliasImpl extends AbstractDockerImpl implements Alias {

  private final SyntaxToken keyword;
  private final SyntaxToken alias;

  public AliasImpl(SyntaxToken keyword, SyntaxToken alias) {
    this.keyword = keyword;
    this.alias = alias;
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }

  @Override
  public SyntaxToken alias() {
    return alias;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword, alias);
  }

  @Override
  public Kind getKind() {
    return Kind.ALIAS;
  }
}
