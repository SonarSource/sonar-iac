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
package org.sonar.iac.arm.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.HasResources;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

abstract class AbstractArmResourceCheck implements IacCheck {

  private final Map<String, List<BiConsumer<CheckContext, ResourceDeclaration>>> resourceTreeConsumers = new HashMap<>();
  private final Map<String, List<Consumer<ContextualResource>>> contextualResourceConsumer = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, this::provideResource);
    registerResourceConsumer();
  }

  protected void provideResource(CheckContext ctx, ResourceDeclaration resource) {
    String resourceType = resource.type().value();
    processResource(ctx, resource, resourceType);
  }

  private void processResource(CheckContext ctx, ResourceDeclaration resource, String resourceType) {
    if (resourceTreeConsumers.containsKey(resourceType)) {
      resourceTreeConsumers.get(resourceType).forEach(consumer -> consumer.accept(ctx, resource));
    }
    if (contextualResourceConsumer.containsKey(resourceType)) {
      ContextualResource symbol = ContextualResource.fromPresent(ctx, resource, resourceType);
      contextualResourceConsumer.get(resourceType).forEach(consumer -> consumer.accept(symbol));
    }

    if (resource instanceof HasResources) {
      for (ResourceDeclaration child : ((HasResources) resource).childResources()) {
        String childResourceType = resourceType + "/" + child.type().value();
        processResource(ctx, child, childResourceType);
      }
    }
  }

  protected abstract void registerResourceConsumer();

  protected void register(String resourceType, BiConsumer<CheckContext, ResourceDeclaration> consumer) {
    resourceTreeConsumers.computeIfAbsent(resourceType, i -> new ArrayList<>()).add(consumer);
  }

  protected void register(List<String> resourceTypes, BiConsumer<CheckContext, ResourceDeclaration> consumer) {
    resourceTypes.forEach(resourceType -> register(resourceType, consumer));
  }

  protected void register(String resourceType, Consumer<ContextualResource> consumer) {
    contextualResourceConsumer.computeIfAbsent(resourceType, i -> new ArrayList<>()).add(consumer);
  }

  protected void register(List<String> resourceTypes, Consumer<ContextualResource> consumer) {
    resourceTypes.forEach(resourceType -> register(resourceType, consumer));
  }
}
