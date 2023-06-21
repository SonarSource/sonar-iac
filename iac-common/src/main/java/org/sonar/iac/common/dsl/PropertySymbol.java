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

import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

public class PropertySymbol<S extends PropertySymbol<S, T>, T extends PropertyTree & Tree> extends Symbol<T> {

  protected PropertySymbol(CheckContext ctx, @Nullable T tree, String name, @Nullable Symbol<? extends Tree> parent) {
    super(ctx, tree, name, parent);
  }

  @Override
  public S reportIfAbsent(String message, SecondaryLocation... secondaries) {
    super.reportIfAbsent(message, secondaries);
    return (S) this;
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree;
  }

  @CheckForNull
  public String asString() {
    return Optional.ofNullable(tree).flatMap(tree -> TextUtils.getValue(tree.value())).orElse(null);
  }
}
