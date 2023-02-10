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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ExecForm;
import org.sonar.iac.docker.tree.api.SeparatedList;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.utils.ArgumentUtils;

public class ExecFormImpl extends AbstractDockerTreeImpl implements ExecForm {

  private final SyntaxToken leftBracket;
  private final SeparatedList<Argument> argumentsWithSeparators;
  private final SyntaxToken rightBracket;

  public ExecFormImpl(SyntaxToken leftBracket, SeparatedList<Argument> argumentsWithSeparators, SyntaxToken rightBracket) {
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

  /**
   * @deprecated To be removed once arguments() methods exist in all implementation and that literals() can be replaced everywhere.
   * For now the method has been transformed to still provide the same data as before.
   */
  @Deprecated(forRemoval = true)
  @Override
  public List<SyntaxToken> literals() {
    List<SyntaxToken> literals = arguments().stream()
      .map(ExecFormImpl::argumentToSyntaxToken)
      .collect(Collectors.toList());
    return literals.contains(null) ? Collections.emptyList() : literals;
  }

  @Nullable
  private static SyntaxToken argumentToSyntaxToken(Argument argument) {
    String value = ArgumentUtils.resolve(argument).value();
    if (value != null) {
      return new SyntaxTokenImpl(value, argument.textRange(), Collections.emptyList());
    }
    return null;
  }

  @Override
  public List<Argument> arguments() {
    return argumentsWithSeparators.elements();
  }

  @Override
  public LiteralListType type() {
    return LiteralListType.EXEC;
  }

  @Override
  public SeparatedList<Argument> argumentsWithSeparators() {
    return argumentsWithSeparators;
  }

  @Override
  public SyntaxToken rightBracket() {
    return rightBracket;
  }
}
