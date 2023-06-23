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

import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.dsl.CommonPropertySymbol;

public class PropertySymbol extends CommonPropertySymbol<PropertySymbol, Property, Expression> {

  public PropertySymbol(CheckContext ctx, @Nullable Property tree, String name, MapSymbol parent) {
    super(ctx, tree, name, parent);
  }

  public static PropertySymbol fromPresent(CheckContext ctx, Property tree, MapSymbol parent) {
    return new PropertySymbol(ctx, tree, tree.key().value(), parent);
  }

  public static PropertySymbol fromAbsent(CheckContext ctx, String name, MapSymbol parent) {
    return new PropertySymbol(ctx, null, name, parent);
  }
}
