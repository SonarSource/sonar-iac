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
package org.sonar.iac.arm.checkdsl;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.HasResources;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.Variable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
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

  @Override
  public ContextualMap<ContextualResource, ResourceDeclaration> reportIfAbsent(String message, List<SecondaryLocation> secondaries) {
    if (parent != null && parent instanceof ContextualResource contextualResource && !contextualResource.isReferencingResource()) {
      super.reportIfAbsent(message, secondaries);
    }
    return this;
  }

  public boolean isReferencingResource() {
    return tree != null && tree.existing() != null;
  }

  @CheckForNull
  @Override
  protected HasTextRange toHighlight() {
    if (tree != null) {
      if (tree.symbolicName() != null) {
        return tree.symbolicName();
      } else {
        return tree.type();
      }
    } else {
      return null;
    }
  }

  public ContextualResource childResourceBy(String type, Predicate<ResourceDeclaration> predicate) {
    return Optional.ofNullable(nestedChildResourceBy(type, predicate))
      .filter(ContextualResource::isPresent)
      .orElseGet(() -> childResourceOutsideOfThisBy(type, predicate));
  }

  private ContextualResource childResourceOutsideOfThisBy(String childType, Predicate<ResourceDeclaration> predicate) {
    var topLevelResources = Optional.ofNullable(tree)
      .map(ArmTreeUtils::getRootNode)
      .map(ContextualResource::getChildResources)
      .orElse(Stream.empty());

    return topLevelResources
      .filter(this::isExternalChildOfThis)
      .filter(it -> TextUtils.isValue(it.type(), this.type + "/" + childType).isTrue())
      .filter(predicate)
      .findFirst()
      .map(it -> new ContextualResource(ctx, it, it.type().value(), this))
      .orElse(ContextualResource.fromAbsent(ctx, childType, this));
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
      return Stream.concat(
        file.statements().stream().filter(ResourceDeclaration.class::isInstance).map(ResourceDeclaration.class::cast),
        file.statements().stream().flatMap(ContextualResource::getChildResources));
    } else if (tree instanceof HasResources hasResources) {
      return Stream.concat(
        hasResources.childResources().stream(),
        hasResources.childResources().stream().flatMap(ContextualResource::getChildResources));
    } else {
      return Stream.empty();
    }
  }

  private boolean isExternalChildOfThis(ResourceDeclaration child) {
    // this can apply for both JSON and Bicep
    boolean isExternalChildByFullResourceName = TextUtils.matchesValue(child.type(), childType -> childType.startsWith(this.type + "/")).isTrue() &&
      TextUtils.matchesValue(child.name(), childName -> childName.startsWith(this.name + "/")).isTrue();
    // Bicep-specific; https://learn.microsoft.com/en-us/azure/azure-resource-manager/bicep/child-resource-name-type#outside-parent-resource
    var explicitParent = child.getResourceProperty("parent")
      .map(Property::value)
      .filter(Variable.class::isInstance)
      .map(it -> ((Variable) it).identifier());
    var symbolicName = this.tree.symbolicName();
    boolean isExternalChildByExplicitRelationship = explicitParent.isPresent() && symbolicName != null && TextUtils.isValue(explicitParent.get(), symbolicName.value()).isTrue();
    return isExternalChildByFullResourceName || isExternalChildByExplicitRelationship;
  }

  /**
   * Returns {@code ContextualProperty} for provided key name.
   * <p>
   * Example:
   * <pre>
   * {@code
   *   {
   *     "key1": "value1"
   *   }
   * }
   * </pre>
   *
   * For call {@code resourceProperty("key1")} it will return {@code ContextualProperty} for {@code "value1"}.
   * <p>
   * For call {@code resourceProperty("unknown")} it will return {@code ContextualProperty} with {@code null} tree.
   */
  public ContextualProperty resourceProperty(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> tree.getResourceProperty(name))
      .map(property -> ContextualProperty.fromPresent(ctx, property, this))
      .orElse(ContextualProperty.fromAbsent(ctx, name, this));
  }
}
