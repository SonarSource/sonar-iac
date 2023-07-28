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

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.TextUtils;

public final class ContextualResource extends ContextualMap<ContextualResource, ResourceDeclaration> {

  public final String type;
  public final String version;

  private ContextualResource(CheckContext ctx, @Nullable ResourceDeclaration tree, String type, @Nullable ContextualMap<?, ?> parent) {
    super(ctx, tree, Optional.ofNullable(tree).map(ResourceDeclaration::name).map(TextTree::value).orElse(null), parent);
    this.type = type;
    this.version = Optional.ofNullable(tree).map(ResourceDeclaration::version).map(TextTree::value).orElse("");
  }

  public static ContextualResource fromPresent(CheckContext ctx, ResourceDeclaration tree) {
    return new ContextualResource(ctx, tree, tree.type().value(), null);
  }

  public static ContextualResource fromPresent(CheckContext ctx, ResourceDeclaration tree, String resourceType) {
    return new ContextualResource(ctx, tree, resourceType, null);
  }

  public static ContextualResource fromAbsent(CheckContext ctx, String resourceType, ContextualMap<?, ?> parent) {
    return new ContextualResource(ctx, null, resourceType, parent);
  }

  @CheckForNull
  @Override
  protected HasTextRange toHighlight() {
    return tree != null ? tree.type() : null;
  }

  public ContextualResource childResourceBy(String type, Predicate<ResourceDeclaration> predicate) {
    return Optional.ofNullable(tree)
      .flatMap(resource -> resource.childResources().stream()
        .filter(it -> TextUtils.isValue(it.type(), type).isTrue())
        .filter(predicate)
        .findFirst())
      .map(it -> new ContextualResource(ctx, it, it.type().value(), this))
      .orElse(ContextualResource.fromAbsent(ctx, type, this));
  }
}
