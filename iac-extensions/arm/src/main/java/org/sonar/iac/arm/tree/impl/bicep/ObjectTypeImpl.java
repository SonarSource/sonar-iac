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
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.ObjectType;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ObjectTypeImpl extends AbstractArmTreeImpl implements ObjectType {
  private final SyntaxToken openingCurlyBracket;
  private final List<ArmTree> properties;
  private final SyntaxToken closingCurlyBracket;

  public ObjectTypeImpl(SyntaxToken openingCurlyBracket, List<ArmTree> properties, SyntaxToken closingCurlyBracket) {
    this.openingCurlyBracket = openingCurlyBracket;
    this.properties = properties;
    this.closingCurlyBracket = closingCurlyBracket;
  }

  @Override
  public List<ArmTree> properties() {
    return properties;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openingCurlyBracket);
    children.addAll(properties);
    children.add(closingCurlyBracket);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT_TYPE;
  }
}
