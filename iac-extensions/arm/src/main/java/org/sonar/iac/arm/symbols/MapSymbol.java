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

import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.dsl.Symbol;

public abstract class MapSymbol<S extends MapSymbol<S, T>, T extends HasProperties & Tree> extends Symbol<MapSymbol<S, T>, T> {

  protected MapSymbol(CheckContext ctx, @Nullable T tree, @Nullable String name, @Nullable MapSymbol<?, ?> parent) {
    super(ctx, tree, name, parent);
  }

  public PropertySymbol property(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, name, Property.class))
      .map(property -> PropertySymbol.fromPresent(ctx, property, this))
      .orElse(PropertySymbol.fromAbsent(ctx, name, this));
  }

  public ObjectSymbol object(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.value(tree, name, ObjectExpression.class))
      .map(objectExpression -> ObjectSymbol.fromPresent(ctx, objectExpression, name, this))
      .orElse(ObjectSymbol.fromAbsent(ctx, name, this));
  }
}
