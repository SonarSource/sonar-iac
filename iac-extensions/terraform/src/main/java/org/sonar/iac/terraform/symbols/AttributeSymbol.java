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
package org.sonar.iac.terraform.symbols;

import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checkdsl.ContextualPropertyTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;

public class AttributeSymbol extends ContextualPropertyTree<AttributeSymbol, AttributeTree, ExpressionTree> {

  protected AttributeSymbol(CheckContext ctx, AttributeTree tree, String name, BlockSymbol parent) {
    super(ctx, tree, name, parent);
  }

  public static AttributeSymbol fromPresent(CheckContext ctx, AttributeTree tree, BlockSymbol parent) {
    return new AttributeSymbol(ctx, tree, tree.key().value(), parent);
  }

  public static AttributeSymbol fromAbsent(CheckContext ctx, String name, BlockSymbol parent) {
    return new AttributeSymbol(ctx, null, name, parent);
  }
}
