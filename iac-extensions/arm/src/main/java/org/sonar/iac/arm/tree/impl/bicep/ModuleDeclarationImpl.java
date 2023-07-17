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
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.ModuleDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import java.util.List;

public class ModuleDeclarationImpl extends AbstractArmTreeImpl implements ModuleDeclaration {
  private final List<Decorator> decorators;
  private final SyntaxToken keyword;
  private final Identifier name;
  private final InterpolatedString type;
  private final SyntaxToken equals;
  private final Expression value;

  public ModuleDeclarationImpl(List<Decorator> decorators, SyntaxToken keyword, Identifier name, InterpolatedString type, SyntaxToken equals, Expression value) {
    this.decorators = decorators;
    this.keyword = keyword;
    this.name = name;
    this.type = type;
    this.equals = equals;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(decorators);
    children.add(keyword);
    children.add(name);
    children.add(type);
    children.add(equals);
    children.add(value);
    return children;
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public InterpolatedString type() {
    return type;
  }

  @Override
  public Expression value() {
    return value;
  }

  @Override
  public List<Decorator> decorators() {
    return decorators;
  }
}
