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
package org.sonar.iac.arm.checkdsl;

import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checkdsl.ContextualTree;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class ContextualObject extends ContextualMap<ContextualObject, ObjectExpression> {
  protected ContextualObject(CheckContext ctx, @Nullable ObjectExpression tree, @Nullable String name, ContextualTree<?, ?> parent) {
    super(ctx, tree, name, parent);
  }

  public static ContextualObject fromPresent(CheckContext ctx, ObjectExpression tree, @Nullable String name, ContextualTree<?, ?> parent) {
    return new ContextualObject(ctx, tree, name, parent);
  }

  public static ContextualObject fromAbsent(CheckContext ctx, @Nullable String name, ContextualMap<?, ?> parent) {
    return new ContextualObject(ctx, null, name, parent);
  }

  public Stream<ContextualProperty> allPropertiesFlattened() {
    return tree.properties().stream()
      .map(Property.class::cast)
      .flatMap(p -> {
        Expression value = p.value();
        if (value instanceof ObjectExpression object) {
          return ContextualObject.fromPresent(ctx, object, p.key().value(), this).allPropertiesFlattened();
        } else {
          return Stream.of(ContextualProperty.fromPresent(ctx, p, this));
        }
      });
  }
}
