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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.HasDecorators;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.Tree;

public class VariableDeclarationImpl extends AbstractDeclaration implements VariableDeclaration, HasDecorators {
  @CheckForNull
  private final List<Decorator> decorators;

  public VariableDeclarationImpl(@Nullable List<Decorator> decorators, SyntaxToken keyword, Identifier identifier, SyntaxToken equals, Expression expression, SyntaxToken newLine) {
    super(keyword, identifier, equals, expression, newLine);
    this.decorators = decorators;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    if (decorators != null) {
      children.addAll(decorators);
    }
    children.addAll(super.children());
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE_DECLARATION;
  }

  @Override
  public Identifier name() {
    return this.identifier;
  }

  @Override
  public Expression value() {
    return this.expression;
  }

  @Override
  public List<Decorator> decorators() {
    return decorators;
  }
}
