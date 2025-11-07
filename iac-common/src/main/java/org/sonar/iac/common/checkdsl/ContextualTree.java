/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.function.Consumer;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;

public abstract class ContextualTree<S extends ContextualTree<S, T>, T extends Tree> {

  public final CheckContext ctx;
  public final @Nullable T tree;
  public final @Nullable String name;
  public final @Nullable ContextualTree<? extends ContextualTree<?, ?>, ? extends Tree> parent;

  protected ContextualTree(CheckContext ctx, @Nullable T tree, @Nullable String name, @Nullable ContextualTree<? extends ContextualTree<?, ?>, ? extends Tree> parent) {
    this.ctx = ctx;
    this.tree = tree;
    this.name = name;
    this.parent = parent;
  }

  public S reportIfAbsent(String message, SecondaryLocation... secondaries) {
    return reportIfAbsent(message, List.of(secondaries));
  }

  public S reportIfAbsent(String message, List<SecondaryLocation> secondaries) {
    if (tree == null && parent != null) {
      parent.report(String.format(message, name), secondaries);
    }
    return (S) this;
  }

  public S report(String message, SecondaryLocation... secondaryLocations) {
    return report(message, List.of(secondaryLocations));
  }

  public S report(String message, List<SecondaryLocation> secondaries) {
    HasTextRange toHighlight = toHighlight();
    if (toHighlight != null) {
      ctx.reportIssue(toHighlight, message, secondaries);
    }
    return (S) this;
  }

  @CheckForNull
  public SecondaryLocation toSecondary(String message) {
    HasTextRange toHighlight = toHighlight();
    if (toHighlight != null) {
      return new SecondaryLocation(toHighlight, message);
    }
    return null;
  }

  @CheckForNull
  protected HasTextRange toHighlight() {
    return tree;
  }

  public boolean isPresent() {
    return tree != null;
  }

  public void ifPresent(Consumer<T> action) {
    if (tree != null) {
      action.accept(tree);
    }
  }

  public boolean isAbsent() {
    return tree == null;
  }

}
