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
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.HereDocument;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.utils.ArgumentUtils;

public class HereDocumentImpl extends AbstractDockerTreeImpl implements HereDocument {

  private final Argument argument;

  public HereDocumentImpl(Argument argument) {
    this.argument = argument;
  }

  @Override
  public List<Tree> children() {
    return List.of(argument);
  }

  @Override
  public Kind getKind() {
    return Kind.HEREDOCUMENT;
  }

  // TODO remove method SONARIAC-579 Remove LiteralList
  @Deprecated(forRemoval = true)
  @Override
  public List<SyntaxToken> literals() {
    SyntaxToken syntaxToken = ArgumentUtils.argumentToSyntaxToken(argument);
    if (syntaxToken != null) {
      return List.of(syntaxToken);
    }
    return List.of();
  }

  @Override
  public List<Argument> arguments() {
    return List.of(argument);
  }

  @Override
  public LiteralListType type() {
    return LiteralListType.HEREDOC;
  }
}
