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
import org.sonar.iac.arm.tree.api.bicep.ForVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.arm.tree.impl.json.ArmHelper;
import org.sonar.iac.common.api.tree.Tree;

public class ForVariableBlockImpl extends AbstractArmTreeImpl implements ForVariableBlock {

  private final SyntaxToken leftParenthesis;
  private final Identifier itemIdentifier;
  private final SyntaxToken comma;
  private final Identifier indexIdentifier;
  private final SyntaxToken rightParenthesis;

  public ForVariableBlockImpl(Identifier itemIdentifier) {
    this.itemIdentifier = itemIdentifier;
    this.leftParenthesis = null;
    this.comma = null;
    this.indexIdentifier = null;
    this.rightParenthesis = null;
  }

  public ForVariableBlockImpl(SyntaxToken leftParenthesis, Identifier itemIdentifier, SyntaxToken comma,
    Identifier indexIdentifier, SyntaxToken rightParenthesis) {
    this.leftParenthesis = leftParenthesis;
    this.itemIdentifier = itemIdentifier;
    this.comma = comma;
    this.indexIdentifier = indexIdentifier;
    this.rightParenthesis = rightParenthesis;
  }

  @Override
  public Identifier itemIdentifier() {
    return itemIdentifier;
  }

  @Override
  public Identifier indexIdentifier() {
    return indexIdentifier;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    ArmHelper.addChildrenIfPresent(children, leftParenthesis);
    children.add(itemIdentifier);
    ArmHelper.addChildrenIfPresent(children, comma);
    ArmHelper.addChildrenIfPresent(children, indexIdentifier);
    ArmHelper.addChildrenIfPresent(children, rightParenthesis);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.FOR_VARIABLE_BLOCK;
  }
}
