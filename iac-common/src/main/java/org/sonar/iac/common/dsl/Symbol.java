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
package org.sonar.iac.common.dsl;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;

public abstract class Symbol<T extends Tree> {

  public final CheckContext ctx;
  public final @Nullable T tree;
  public final String name;
  public final @Nullable Symbol<? extends Tree> parent;

  protected Symbol(CheckContext ctx, @Nullable T tree, String name, @Nullable Symbol<? extends Tree> parent) {
    this.ctx = ctx;
    this.tree = tree;
    this.name = name;
    this.parent = parent;
  }

  public Symbol<T> reportIfAbsent(String message, SecondaryLocation... secondaries) {
    if (tree == null && parent != null) {
      parent.report(String.format(message, name), List.of(secondaries));
    }
    return this;
  }

  public Symbol<T> report(String message, SecondaryLocation... secondaryLocations) {
    return report(message, List.of(secondaryLocations));
  }

  public Symbol<T> report(String message, List<SecondaryLocation> secondaries) {
    HasTextRange toHighlight = toHighlight();
    if (toHighlight != null) {
      ctx.reportIssue(toHighlight, message, secondaries);
    }
    return this;
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
  protected abstract HasTextRange toHighlight();

  public boolean isPresent() {
    return tree != null;
  }

  public boolean isAbsent() {
    return tree == null;
  }

}
