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
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class TypeDeclarationImpl extends AbstractArmTreeImpl implements TypeDeclaration {

  private final List<Decorator> decorators;
  private final SyntaxToken keyword;
  private final Identifier name;
  private final SyntaxToken equ;
  private final TypeExpressionAble typeExpression;

  public TypeDeclarationImpl(List<Decorator> decorators, SyntaxToken keyword, Identifier name, SyntaxToken equ, TypeExpressionAble typeExpression) {
    this.decorators = decorators;
    this.keyword = keyword;
    this.name = name;
    this.equ = equ;
    this.typeExpression = typeExpression;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(decorators);
    children.add(keyword);
    children.add(name);
    children.add(equ);
    children.add(typeExpression);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.TYPE_DECLARATION;
  }

  @Override
  public Identifier declaratedName() {
    return name;
  }

  @Override
  public TypeExpressionAble type() {
    return typeExpression;
  }

  @Override
  public List<Decorator> decorators() {
    return decorators;
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }
}
