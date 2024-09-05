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

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.HasResources;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.checks.TextUtils;

public final class ContextualResource extends ContextualMap<ContextualResource, ResourceDeclaration> {

  public final String type;
  @Nullable
  public final Expression version;

  private ContextualResource(CheckContext ctx, @Nullable ResourceDeclaration tree, String type, @Nullable ContextualMap<?, ?> parent) {
    super(ctx, tree,
      Optional.ofNullable(tree).map(ResourceDeclaration::name).flatMap(TextUtils::getValue).orElse(null),
      parent);
    this.type = type;
    this.version = Optional.ofNullable(tree).map(ResourceDeclaration::version).orElse(null);
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

  public boolean isReferencingResource() {
    return tree != null && tree.existing() != null;
  }

  @CheckForNull
  @Override
  protected HasTextRange toHighlight() {
    return tree != null ? tree.type() : null;
  }

  public ContextualResource childResourceBy(String type, Predicate<ResourceDeclaration> predicate) {
    return Optional.ofNullable(nestedChildResourceBy(type, predicate))
      .filter(ContextualResource::isPresent)
      .orElseGet(() -> childResourceOutsideOfThisBy(type, predicate));
  }

  private ContextualResource childResourceOutsideOfThisBy(String type, Predicate<ResourceDeclaration> predicate) {
    var topLevelResources = Optional.ofNullable(tree)
      .map(ResourceDeclaration::parent)
      .map(ContextualResource::getChildResources)
      .orElse(Stream.empty());

    return topLevelResources
      .filter(this::isExternalChildOfThis)
      .filter(it -> TextUtils.isValue(it.type(), this.type + "/" + type).isTrue())
      .filter(predicate)
      .findFirst()
      .map(it -> new ContextualResource(ctx, it, it.type().value(), this))
      .orElse(ContextualResource.fromAbsent(ctx, type, this));
  }

  private ContextualResource nestedChildResourceBy(String type, Predicate<ResourceDeclaration> predicate) {
    return Optional.ofNullable(tree)
      .flatMap(resource -> resource.childResources().stream()
        .filter(it -> TextUtils.isValue(it.type(), type).isTrue())
        .filter(predicate)
        .findFirst())
      .map(it -> new ContextualResource(ctx, it, it.type().value(), this))
      .orElse(ContextualResource.fromAbsent(ctx, type, this));
  }

  private static Stream<ResourceDeclaration> getChildResources(ArmTree tree) {
    if (tree instanceof File file) {
      return file.statements().stream().filter(ResourceDeclaration.class::isInstance).map(ResourceDeclaration.class::cast);
    } else if (tree instanceof ObjectExpression objectExpression) {
      return objectExpression.nestedResources().stream();
    } else {
      return ((HasResources) tree).childResources().stream();
    }
  }

  private boolean isExternalChildOfThis(ResourceDeclaration child) {
    return TextUtils.matchesValue(child.type(), childType -> childType.startsWith(this.type + "/")).isTrue() &&
      TextUtils.matchesValue(child.name(), childName -> childName.startsWith(this.name + "/")).isTrue();
  }
}
