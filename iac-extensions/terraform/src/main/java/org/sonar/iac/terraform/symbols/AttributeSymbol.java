/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
