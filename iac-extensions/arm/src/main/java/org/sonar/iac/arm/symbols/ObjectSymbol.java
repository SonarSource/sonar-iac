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
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.dsl.Symbol;

public class ObjectSymbol extends HasPropertiesSymbol<ObjectSymbol, ObjectExpression> {

  public static ObjectSymbol fromPresent(CheckContext ctx, ObjectExpression tree, @Nullable String name,
    @Nullable HasPropertiesSymbol<? extends HasPropertiesSymbol<?, ?>, ? extends HasProperties> parent) {
    return new ObjectSymbol(ctx, tree, name, parent);
  }

  public static ObjectSymbol fromAbsent(CheckContext ctx, @Nullable String name,
    @Nullable HasPropertiesSymbol<? extends HasPropertiesSymbol<?, ?>, ? extends HasProperties> parent) {
    return new ObjectSymbol(ctx, null, name, parent);
  }

  protected ObjectSymbol(CheckContext ctx, @Nullable ObjectExpression tree, @Nullable String name, @Nullable Symbol<? extends Tree> parent) {
    super(ctx, tree, name, parent);
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree;
  }
}
