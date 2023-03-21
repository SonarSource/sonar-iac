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
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.symbols.Symbol;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EncapsulatedVariable;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class EncapsulatedVariableImpl extends AbstractDockerTreeImpl implements EncapsulatedVariable {

  private final SyntaxToken openDollarCurly;
  private final SyntaxToken identifier;
  private final SyntaxToken modifierSeparator;
  private final Argument modifier;
  private final SyntaxToken closeCurly;
  private Symbol symbol;

  public EncapsulatedVariableImpl(SyntaxToken openDollarCurly,
    SyntaxToken identifier,
    @Nullable SyntaxToken modifierSeparator,
    @Nullable Argument modifier,
    SyntaxToken closeCurly) {
    this.openDollarCurly = openDollarCurly;
    this.identifier = identifier;
    this.modifierSeparator = modifierSeparator;
    this.modifier = modifier;
    this.closeCurly = closeCurly;
  }

  @Override
  public String identifier() {
    return identifier.value();
  }

  @Nullable
  @Override
  public String modifierSeparator() {
    return modifierSeparator != null ? modifierSeparator.value() : null;
  }

  @Nullable
  @Override
  public Argument modifier() {
    return modifier;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openDollarCurly);
    children.add(identifier);
    if (modifierSeparator != null) {
      children.add(modifierSeparator);
    }
    if (modifier != null) {
      children.add(modifier);
    }
    children.add(closeCurly);
    return children;
  }

  @Override
  public Kind getKind() {
    return DockerTree.Kind.ENCAPSULATED_VARIABLE;
  }

  @Nullable
  @Override
  public Symbol symbol() {
    return symbol;
  }

  @Override
  public void setSymbol(Symbol symbol) {
    if (this.symbol != null) {
      throw new IllegalArgumentException("A symbol is already set");
    }
    this.symbol = symbol;
  }
}
