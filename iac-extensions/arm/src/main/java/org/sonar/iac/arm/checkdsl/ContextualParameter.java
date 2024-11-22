/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checkdsl.ContextualTree;

public class ContextualParameter extends ContextualTree<ContextualParameter, ParameterDeclaration> {

  public <S extends ContextualTree<S, T>, T extends ParameterDeclaration & Tree> ContextualParameter(
    CheckContext ctx,
    ParameterDeclaration tree,
    String name,
    @Nullable ContextualTree<S, T> parent) {
    super(ctx, tree, name, parent);
  }

  public static <S extends ContextualTree<S, T>, T extends ParameterDeclaration & Tree> ContextualParameter fromPresent(
    CheckContext ctx,
    ParameterDeclaration tree,
    @Nullable ContextualTree<S, T> parent) {
    return new ContextualParameter(ctx, tree, tree.declaratedName().value(), parent);
  }

  public <S extends ContextualTree<S, T>, T extends ParameterDeclaration & Tree> S reportIf(Predicate<Expression> predicate, String message, SecondaryLocation... secondaries) {
    Expression defaultValue = tree.defaultValue();
    if (defaultValue != null && predicate.test(defaultValue)) {
      return (S) report(message, List.of(secondaries));
    }
    return (S) this;
  }

  @Override
  @CheckForNull
  protected HasTextRange toHighlight() {
    Expression defaultValue = tree.defaultValue();
    return defaultValue != null ? defaultValue : tree;
  }
}
