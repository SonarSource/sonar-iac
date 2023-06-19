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
package org.sonar.iac.arm.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.sonar.iac.arm.tree.api.HasResources;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

abstract class AbstractArmResourceCheck implements IacCheck {

  private final Map<String, List<BiConsumer<CheckContext, ResourceDeclaration>>> resourceConsumers = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, this::provideResource);
    registerResourceConsumer();
  }

  protected void provideResource(CheckContext ctx, ResourceDeclaration resource) {
    String resourceType = resource.type().value();
    processResource(ctx, resource, resourceType);
  }

  private void provideChildResources(CheckContext ctx, HasResources hasResource, String parentType) {
    for (ResourceDeclaration resource : hasResource.childResources()) {
      String resourceType = parentType + "/" + resource.type().value();
      processResource(ctx, resource, resourceType);
    }
  }

  private void processResource(CheckContext ctx, ResourceDeclaration resource, String resourceType) {
    if (resourceConsumers.containsKey(resourceType)) {
      resourceConsumers.get(resourceType).forEach(consumer -> consumer.accept(ctx, resource));
    }
    if (resource instanceof HasResources) {
      provideChildResources(ctx, (HasResources) resource, resourceType);
    }
  }

  protected abstract void registerResourceConsumer();

  protected void register(String resourceType, BiConsumer<CheckContext, ResourceDeclaration> consumer) {
    resourceConsumers.computeIfAbsent(resourceType, i -> new ArrayList<>()).add(consumer);
  }
}
