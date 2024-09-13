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
package org.sonar.iac.docker.tree.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ExpandableStringLiteralImpl extends AbstractDockerTreeImpl implements ExpandableStringLiteral {

  private final SyntaxToken openDoubleQuote;

  private final List<Expression> elements;
  private final SyntaxToken closeDoubleQuote;

  public ExpandableStringLiteralImpl(SyntaxToken openDoubleQuote, List<Expression> elements, SyntaxToken closeDoubleQuote) {
    this.openDoubleQuote = openDoubleQuote;
    this.elements = elements;
    this.closeDoubleQuote = closeDoubleQuote;
  }

  @Override
  public List<Expression> expressions() {
    return elements;
  }

  @Override
  public SyntaxToken getOpenDoubleQuote() {
    return openDoubleQuote;
  }

  @Override
  public SyntaxToken getCloseDoubleQuote() {
    return closeDoubleQuote;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openDoubleQuote);
    children.addAll(elements);
    children.add(closeDoubleQuote);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.EXPANDABLE_STRING_LITERAL;
  }

  @Override
  public String toString() {
    return openDoubleQuote + elements.stream().map(Expression::toString).collect(Collectors.joining()) + closeDoubleQuote;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExpandableStringLiteralImpl that)) {
      return false;
    }
    return Objects.equals(openDoubleQuote, that.openDoubleQuote) && Objects.equals(elements, that.elements) && Objects.equals(closeDoubleQuote, that.closeDoubleQuote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(openDoubleQuote, elements, closeDoubleQuote);
  }
}
