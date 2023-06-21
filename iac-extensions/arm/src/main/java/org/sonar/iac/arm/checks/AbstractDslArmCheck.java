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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.sonar.iac.arm.symbols.ResourceSymbol;
import org.sonar.iac.arm.tree.api.HasResources;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

abstract class AbstractDslArmCheck implements IacCheck {

  private final Map<String, List<Consumer<ResourceSymbol>>> resourceConsumers = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, this::provideResource);
    registerResourceConsumer();
  }

  protected void provideResource(CheckContext ctx, ResourceDeclaration resource) {
    ResourceSymbol symbol = ResourceSymbol.fromPresent(ctx, resource);
    processResource(symbol);
  }

  private void processResource(ResourceSymbol symbol) {
    if (resourceConsumers.containsKey(symbol.type)) {
      resourceConsumers.get(symbol.type).forEach(consumer -> consumer.accept(symbol));
    }
    if (symbol.tree instanceof HasResources) {
      for (ResourceDeclaration child : ((HasResources) symbol.tree).childResources()) {
        processResource(ResourceSymbol.fromParent(symbol, child));
      }
    }
  }

  protected abstract void registerResourceConsumer();

  protected void register(Collection<String> resourceTypes, Consumer<ResourceSymbol> consumer) {
    resourceTypes.forEach(resourceType -> register(resourceType, consumer));
  }

  protected void register(String resourceType, Consumer<ResourceSymbol> consumer) {
    resourceConsumers.computeIfAbsent(resourceType, i -> new ArrayList<>()).add(consumer);
  }
}
