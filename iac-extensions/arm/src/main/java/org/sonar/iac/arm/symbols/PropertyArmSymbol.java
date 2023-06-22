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
package org.sonar.iac.arm.symbols;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.dsl.PropertySymbol;
import org.sonar.iac.common.dsl.Symbol;

public class PropertyArmSymbol extends PropertySymbol<PropertyArmSymbol, Property> {
  protected PropertyArmSymbol(CheckContext ctx, @Nullable Property tree, String name, @Nullable Symbol<? extends Tree> parent) {
    super(ctx, tree, name, parent);
  }

  public static PropertyArmSymbol fromPresent(CheckContext ctx, Property tree, @Nullable HasPropertiesSymbol<? extends HasPropertiesSymbol<?, ?>, ? extends HasProperties> parent) {
    return new PropertyArmSymbol(ctx, tree, tree.key().value(), parent);
  }

  public static PropertyArmSymbol fromAbsent(CheckContext ctx, String name, @Nullable HasPropertiesSymbol<? extends HasPropertiesSymbol<?, ?>, ? extends HasProperties> parent) {
    return new PropertyArmSymbol(ctx, null, name, parent);
  }

  public PropertyArmSymbol reportIf(Predicate<Expression> predicate, String message, SecondaryLocation... secondaries) {
    if (tree != null && predicate.test(tree.value())) {
      return (PropertyArmSymbol) report(message, List.of(secondaries));
    }
    return this;
  }
}
