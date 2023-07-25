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
    return new ContextualParameter(ctx, tree, tree.identifier().value(), parent);
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
    return tree.defaultValue();
  }
}
