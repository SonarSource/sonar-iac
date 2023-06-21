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
package org.sonar.iac.terraform.symbols;

import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.dsl.Symbol;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;

import static org.sonar.iac.terraform.checks.utils.TerraformUtils.attributeAccessToString;

public class ReferenceSymbol extends Symbol<AttributeTree> {

  private AttributeAccessTree reference;

  private ReferenceSymbol(CheckContext ctx, AttributeTree tree, String name, BlockSymbol parent, AttributeAccessTree reference) {
    super(ctx, tree, name, parent);
    if (reference != null) {
      this.reference = reference;
    }
  }

  public static ReferenceSymbol fromPresent(CheckContext ctx, AttributeTree tree, BlockSymbol parent) {
    if (tree.value().is(TerraformTree.Kind.ATTRIBUTE_ACCESS)) {
      return new ReferenceSymbol(ctx, tree, tree.key().value(), parent, (AttributeAccessTree) tree.value());
    }
    return ReferenceSymbol.fromAbsent(ctx, tree.key().value(), parent);
  }

  public static ReferenceSymbol fromAbsent(CheckContext ctx, String name, BlockSymbol parent) {
    return new ReferenceSymbol(ctx, null, name, parent, null);
  }

  public BlockSymbol resolve(Map<String, BlockSymbol> symbolTable) {
    return Optional.ofNullable(tree)
      .map(tree -> symbolTable.get(attributeAccessToString(reference)))
      .orElse(BlockSymbol.fromAbsent(ctx, "unknown", null));
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree;
  }
}
