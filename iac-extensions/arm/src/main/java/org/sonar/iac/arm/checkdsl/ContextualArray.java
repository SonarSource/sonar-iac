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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checkdsl.ContextualListTree;

public class ContextualArray extends ContextualListTree<ContextualArray, ArrayExpression, Expression> {
  protected ContextualArray(CheckContext ctx, @Nullable ArrayExpression tree, String name, ContextualMap<?, ?> parent, List<Expression> items) {
    super(ctx, tree, name, parent, items);
  }

  public static ContextualArray fromPresent(CheckContext ctx, ArrayExpression tree, String name, ContextualMap<?, ?> parent) {
    return new ContextualArray(ctx, tree, name, parent, tree.elements());
  }

  public static ContextualArray fromAbsent(CheckContext ctx, String name, ContextualMap<?, ?> parent) {
    return new ContextualArray(ctx, null, name, parent, Collections.emptyList());
  }

  public Stream<ContextualObject> objects() {
    return items.stream()
      .filter(ObjectExpression.class::isInstance)
      .map(object -> ContextualObject.fromPresent(ctx, (ObjectExpression) object, null, this));
  }
}
