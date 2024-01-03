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
package org.sonar.iac.common.checkdsl;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

public abstract class ContextualPropertyTree<S extends ContextualPropertyTree<S, T, E>, T extends PropertyTree & Tree, E extends Tree> extends ContextualTree<S, T> {
  protected ContextualPropertyTree(CheckContext ctx, @Nullable T tree, @Nullable String name, @Nullable ContextualTree<? extends ContextualTree<?, ?>, ? extends Tree> parent) {
    super(ctx, tree, name, parent);
  }

  public S reportIf(Predicate<E> predicate, String message, SecondaryLocation... secondaries) {
    if (tree != null && predicate.test((E) tree.value())) {
      return report(message, List.of(secondaries));
    }
    return (S) this;
  }

  public boolean is(Predicate<E> predicate) {
    if (tree != null) {
      return predicate.test((E) tree.value());
    } else {
      return predicate.test(null);
    }
  }

  @CheckForNull
  public String asString() {
    return Optional.ofNullable(tree).flatMap(tree -> TextUtils.getValue(tree.value())).orElse(null);
  }

}
