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
import org.sonar.iac.docker.tree.api.ExecForm;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.SeparatedList;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ExecFormImpl extends AbstractDockerTreeImpl implements ExecForm {

  private final SyntaxToken leftBracket;
  private final SeparatedList<ExpandableStringLiteral> expressionsWithSeparators;
  private final SyntaxToken rightBracket;

  public ExecFormImpl(SyntaxToken leftBracket, SeparatedList<ExpandableStringLiteral> expressionsWithSeparators, SyntaxToken rightBracket) {
    this.leftBracket = leftBracket;
    this.expressionsWithSeparators = expressionsWithSeparators;
    this.rightBracket = rightBracket;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(leftBracket);
    result.addAll(expressionsWithSeparators.elementsAndSeparators());
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
    throw new UnsupportedOperationException("Method to be removed from LiteralList interface once SONARIAC-541 and SONARIAC-572 are done.");
  }

  @Override
  public List<Argument> arguments() {
    List<Argument> result = new ArrayList<>();
    for (ExpandableStringLiteral element : expressionsWithSeparators.elements()) {
      result.add(new ArgumentImpl(List.of(element)));
    }
    return result;
  }

  @Override
  public LiteralListType type() {
    return LiteralListType.EXEC;
  }

  @Override
  public SeparatedList<ExpandableStringLiteral> expressionsWithSeparators() {
    return expressionsWithSeparators;
  }

  @Override
  public SyntaxToken rightBracket() {
    return rightBracket;
  }
}
