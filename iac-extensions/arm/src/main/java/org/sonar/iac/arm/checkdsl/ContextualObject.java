/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.checkdsl;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checkdsl.ContextualTree;
import org.sonar.iac.common.checks.Trilean;

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

  @Override
  public ContextualMap<ContextualObject, ObjectExpression> reportIfAbsent(String message, List<SecondaryLocation> secondaries) {
    if (isResourceReferencing().isTrue()) {
      return this;
    }
    return super.reportIfAbsent(message, secondaries);
  }

  private Trilean isResourceReferencing() {
    ContextualTree<?, ?> parentTree = this.parent;
    while (parentTree != null) {
      if (parentTree instanceof ContextualResource resource) {
        return Trilean.fromBoolean(resource.isReferencingResource());
      }
      parentTree = parentTree.parent;
    }
    return Trilean.UNKNOWN;
  }
}
