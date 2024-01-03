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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TupleItem;
import org.sonar.iac.arm.tree.api.bicep.TupleType;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class TupleTypeImpl extends AbstractArmTreeImpl implements TupleType {
  private final SyntaxToken openingBracket;
  private final List<TupleItem> tupleItems;
  private final SyntaxToken closingBracket;

  public TupleTypeImpl(SyntaxToken openingBracket, List<TupleItem> tupleItems, SyntaxToken closingBracket) {
    this.openingBracket = openingBracket;
    this.tupleItems = tupleItems;
    this.closingBracket = closingBracket;
  }

  @Override
  public List<TupleItem> items() {
    return tupleItems;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openingBracket);
    children.addAll(tupleItems);
    children.add(closingBracket);
    return children;
  }
}
