/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ExecForm;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ExecFormImpl extends AbstractDockerTreeImpl implements ExecForm {

  private final SyntaxToken leftBracket;
  private final SeparatedList<Argument, SyntaxToken> argumentsWithSeparators;
  private final SyntaxToken rightBracket;

  public ExecFormImpl(SyntaxToken leftBracket, SeparatedList<Argument, SyntaxToken> argumentsWithSeparators, SyntaxToken rightBracket) {
    this.leftBracket = leftBracket;
    this.argumentsWithSeparators = argumentsWithSeparators;
    this.rightBracket = rightBracket;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(leftBracket);
    result.addAll(argumentsWithSeparators.elementsAndSeparators());
    result.add(rightBracket);
    return result;
  }

  @Override
  public Kind getKind() {
    return Kind.EXEC_FORM;
  }

  @Override
  public SyntaxToken leftBracket() {
    return leftBracket;
  }

  @Override
  public List<Argument> arguments() {
    return argumentsWithSeparators.elements();
  }

  @Override
  public SeparatedList<Argument, SyntaxToken> argumentsWithSeparators() {
    return argumentsWithSeparators;
  }

  @Override
  public SyntaxToken rightBracket() {
    return rightBracket;
  }
}
