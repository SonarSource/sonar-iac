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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;

public class AttributeSymbol extends Symbol<AttributeTree> {

  protected AttributeSymbol(CheckContext ctx, AttributeTree tree, String name, BlockSymbol parent) {
    super(ctx, tree, name, parent);
  }

  public static AttributeSymbol fromPresent(CheckContext ctx, AttributeTree tree, BlockSymbol parent) {
    return new AttributeSymbol(ctx, tree, tree.key().value(), parent);
  }

  public static AttributeSymbol fromAbsent(CheckContext ctx, String name, BlockSymbol parent) {
    return new AttributeSymbol(ctx, null, name, parent);
  }

  public AttributeSymbol reportIf(Predicate<ExpressionTree> predicate, String message, SecondaryLocation... secondaries) {
    if (tree != null && predicate.test(tree.value())) {
      return (AttributeSymbol) report(message, List.of(secondaries));
    }
    return this;
  }

  public boolean is(Predicate<ExpressionTree> predicate) {
    if (tree != null) {
      return predicate.test(tree.value());
    } else {
      return predicate.test(null);
    }
  }

  @Override
  public AttributeSymbol reportIfAbsent(String message, SecondaryLocation... secondaries) {
    super.reportIfAbsent(message, secondaries);
    return this;
  }

  @CheckForNull
  public String asString() {
    return Optional.ofNullable(tree).flatMap(tree -> TextUtils.getValue(tree.value())).orElse(null);
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree;
  }


}
