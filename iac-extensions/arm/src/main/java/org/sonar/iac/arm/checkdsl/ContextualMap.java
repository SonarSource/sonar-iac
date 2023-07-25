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
package org.sonar.iac.arm.checkdsl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checkdsl.ContextualTree;
import org.sonar.iac.common.checks.PropertyUtils;

public abstract class ContextualMap<S extends ContextualMap<S, T>, T extends HasProperties & Tree> extends ContextualTree<ContextualMap<S, T>, T> {

  protected ContextualMap(CheckContext ctx, @Nullable T tree, @Nullable String name, @Nullable ContextualTree<?, ?> parent) {
    super(ctx, tree, name, parent);
  }

  /**
   * Returns {@code ContextualProperty} for provided key name.
   * <p>
   * Example:
   * <pre>
   * {@code
   *   {
   *     properties: {
   *       "key1": "value1",
   *       "key2": "value2"
   *     }
   *   }
   * }
   * </pre>
   *
   * For call {@code property("key1")} it will return {@code ContextualProperty} for {@code "value1"}.
   * <p>
   * For call {@code property("unknown")} it will return {@code ContextualProperty} with {@code null} tree.
   */
  public ContextualProperty property(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, name, Property.class))
      .map(property -> ContextualProperty.fromPresent(ctx, property, this))
      .orElse(ContextualProperty.fromAbsent(ctx, name, this));
  }

  /**
   * Returns {@code ContextualObject} for provided key name.
   * <p>
   * Example:
   * <pre>
   * {@code
   *   {
   *     properties: {
   *       "key1": {
   *         "key11": {
   *           "key111": "value111"
   *         }
   *       }
   *     }
   *   }
   * }
   * </pre>
   *
   * For call {@code object("key1")} it will return {@code ContextualObject} for {@code {"key11": ...}}.
   * <p>
   * The call can be chained: {@code object("key1").object("key11)} will return {@code {"key111": ...}}.
   */
  public ContextualObject object(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.value(tree, name, ObjectExpression.class))
      .map(objectExpression -> ContextualObject.fromPresent(ctx, objectExpression, name, this))
      .orElse(ContextualObject.fromAbsent(ctx, name, this));
  }

  public List<ContextualObject> objectsByPath(String path) {
    if (tree == null) {
      return Collections.emptyList();
    }

    return ArmTreeUtils.resolveProperties(path, tree).stream()
      .filter(ObjectExpression.class::isInstance)
      .map(expression -> ContextualObject.fromPresent(ctx, (ObjectExpression) expression, name, null))
      .collect(Collectors.toList());
  }

  /**
   * Returns {@code ContextualArray} for provided key name.
   * <p>
   * Example:
   * <pre>
   * {@code
   *   {
   *     properties: {
   *       "key": [
   *         {
   *           "key1": "value1"
   *         },
   *         {
   *           "key2": "value2"
   *         }
   *       ]
   *     }
   *   }
   * }
   * </pre>
   * For call {@code list("key")} it will return {@code ContextualArray} for {@code {"key1":...}} and {@code {"key2":...}}.
   * <p>
   * For call {@code property("unknown")} it will return {@code ContextualProperty} with {@code null} tree.
   */
  public ContextualArray list(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.value(tree, name, ArrayExpression.class))
      .map(arrayExpression -> ContextualArray.fromPresent(ctx, arrayExpression, name, this))
      .orElse(ContextualArray.fromAbsent(ctx, name, this));
  }
}
