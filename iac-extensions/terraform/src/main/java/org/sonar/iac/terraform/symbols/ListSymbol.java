/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.TupleTree;

public class ListSymbol extends Symbol<AttributeTree> {

  private final List<ExpressionTree> items;

  private ListSymbol(CheckContext ctx, AttributeTree tree, String name, BlockSymbol parent, List<ExpressionTree> items) {
    super(ctx, tree, name, parent);
    this.items = items;
  }

  public static ListSymbol fromPresent(CheckContext ctx, AttributeTree tree, BlockSymbol parent) {
    // Declare list as absent if attribute is present but not of type list
    if (tree.value() instanceof TupleTree) {
      return new ListSymbol(ctx, tree, tree.key().value(), parent, ((TupleTree) tree.value()).elements().trees());
    }
    return fromAbsent(ctx, tree.key().value(), parent);
  }

  public static ListSymbol fromAbsent(CheckContext ctx, String name, BlockSymbol parent) {
    return new ListSymbol(ctx, null, name, parent, Collections.emptyList());
  }

  public ListSymbol reportItemIf(Predicate<ExpressionTree> predicate, String message, SecondaryLocation... secondaryLocations) {
    items.stream().filter(predicate).forEach(item -> ctx.reportIssue(item, message, List.of(secondaryLocations)));
    return this;
  }

  public Stream<ExpressionTree> getItemIf(Predicate<ExpressionTree> predicate) {
    return items.stream().filter(predicate);
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree != null ? tree.key() : null;
  }
}
