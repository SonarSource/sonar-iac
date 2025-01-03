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
package org.sonar.iac.common.checkdsl;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;

public class ContextualListTree<S extends ContextualListTree<S, T, E>, T extends Tree, E extends Tree> extends ContextualTree<S, T> {

  protected final List<E> items;

  protected ContextualListTree(CheckContext ctx, @Nullable T tree, String name, @Nullable ContextualTree<?, ?> parent, List<E> items) {
    super(ctx, tree, name, parent);
    this.items = items;
  }

  public S reportItemIf(Predicate<E> predicate, String message, SecondaryLocation... secondaryLocations) {
    items.stream().filter(predicate).forEach(item -> ctx.reportIssue(item, message, List.of(secondaryLocations)));
    return (S) this;
  }

  public S reportIfEmpty(String message, SecondaryLocation... secondaryLocations) {
    if (isEmpty()) {
      report(message, secondaryLocations);
    }
    return (S) this;
  }

  public Stream<E> getItemIf(Predicate<E> predicate) {
    return items.stream().filter(predicate);
  }

  public boolean isEmpty() {
    return tree != null && items.isEmpty();
  }

}
