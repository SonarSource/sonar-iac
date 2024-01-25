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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checkdsl.ContextualPropertyTree;
import org.sonar.iac.common.checkdsl.ContextualTree;
import org.sonar.iac.common.checks.Trilean;

import java.util.List;

public class ContextualProperty extends ContextualPropertyTree<ContextualProperty, Property, Expression> {

  public <S extends ContextualMap<S, T>, T extends HasProperties & Tree> ContextualProperty(
    CheckContext ctx,
    @Nullable Property tree,
    String name,
    ContextualMap<S, T> parent) {
    super(ctx, tree, name, parent);
  }

  public static <S extends ContextualMap<S, T>, T extends HasProperties & Tree> ContextualProperty fromPresent(
    CheckContext ctx,
    Property tree,
    ContextualMap<S, T> parent) {
    return new ContextualProperty(ctx, tree, tree.key().value(), parent);
  }

  public static <S extends ContextualMap<S, T>, T extends HasProperties & Tree> ContextualProperty fromAbsent(
    CheckContext ctx,
    String name,
    ContextualMap<S, T> parent) {
    return new ContextualProperty(ctx, null, name, parent);
  }

  @Override
  public ContextualProperty reportIfAbsent(String message, List<SecondaryLocation> secondaries) {
    if (isResourceReferencing().isTrue()) {
      return this;
    }
    return super.reportIfAbsent(message, secondaries);
  }

  private Trilean isResourceReferencing() {
    ContextualTree<?, ?> parentTree = this.parent;
    while (parentTree != null) {
      if (parentTree instanceof ContextualResource) {
        return ((ContextualResource) parentTree).isReferencingResource() ? Trilean.TRUE : Trilean.FALSE;
      }
      parentTree = parentTree.parent;
    }
    return Trilean.UNKNOWN;
  }

  @CheckForNull
  public Expression valueOrNull() {
    if (tree != null) {
      return tree.value();
    }
    return null;
  }
}
