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
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.dsl.MapSymbol;
import org.sonar.iac.common.dsl.Symbol;

public class HasPropertiesSymbol<S extends HasPropertiesSymbol<S, T>, T extends ArmTree & HasProperties> extends MapSymbol<HasPropertiesSymbol<S, T>, T> {
  protected HasPropertiesSymbol(CheckContext ctx, @Nullable T tree, @Nullable String name, @Nullable Symbol<? extends Tree> parent) {
    super(ctx, tree, name, parent);
  }

  public PropertyArmSymbol property(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, name, Property.class))
      .map(property -> PropertyArmSymbol.fromPresent(ctx, property, this))
      .orElse(PropertyArmSymbol.fromAbsent(ctx, name, this));
  }

  public S reportIf(Predicate<T> predicate, String message, SecondaryLocation... secondaries) {
    if (tree != null && predicate.test(tree)) {
      return (S) report(message, List.of(secondaries));
    }
    return (S) this;
  }

  public ObjectSymbol object(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.value(tree, name, ObjectExpression.class))
      .map(objectExpression -> ObjectSymbol.fromPresent(ctx, objectExpression, name, this))
      .orElse(ObjectSymbol.fromAbsent(ctx, name, this));
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return null;
  }
}
