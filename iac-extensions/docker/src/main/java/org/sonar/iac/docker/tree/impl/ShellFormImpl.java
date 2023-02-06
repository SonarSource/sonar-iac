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

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ShellForm;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ShellFormImpl extends AbstractDockerTreeImpl implements ShellForm {

  private final List<SyntaxToken> literals;

  public ShellFormImpl(List<SyntaxToken> literals) {
    this.literals = literals;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(literals);
  }

  @Override
  public Kind getKind() {
    return Kind.SHELL_FORM;
  }

  @Override
  public List<SyntaxToken> literals() {
    return literals;
  }

  // TODO : SONARIAC-541 adapt ShellForm to use Argument
  @Override
  public List<Argument> arguments() {
    throw new UnsupportedOperationException("TODO SONARIAC-541");
  }

  @Override
  public LiteralListType type() {
    return LiteralListType.SHELL;
  }
}
