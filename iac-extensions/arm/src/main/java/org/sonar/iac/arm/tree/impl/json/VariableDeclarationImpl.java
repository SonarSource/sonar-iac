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
package org.sonar.iac.arm.tree.impl.json;

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class VariableDeclarationImpl extends AbstractArmTreeImpl implements VariableDeclaration {

  private final Identifier name;
  private final Expression value;

  public VariableDeclarationImpl(Identifier name, Expression value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public Identifier declaratedName() {
    return name;
  }

  @Override
  public Expression value() {
    return value;
  }

  @Override
  public List<Tree> children() {
    return List.of(name, value);
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE_DECLARATION;
  }
}
