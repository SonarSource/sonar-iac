/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;

public abstract class AbstractNewResourceCheck implements IacCheck {

  private final Map<String, List<Consumer<ResourceSymbol>>> resourceConsumers = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, this::provideResource);
    registerResourceConsumer();
  }

  protected abstract void registerResourceConsumer();

  protected void provideResource(CheckContext ctx, BlockTree blockTree) {
    if (isResource(blockTree)) {
      ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, blockTree);
      if (resourceConsumers.containsKey(resource.type)) {
        resourceConsumers.get(resource.type).forEach(consumer -> consumer.accept(resource));
      }
    }
  }

  protected void register(String resourceName, Consumer<ResourceSymbol> consumer) {
    resourceConsumers.computeIfAbsent(resourceName, i -> new ArrayList<>()).add(consumer);
  }

  protected void register(List<String> resourceNames, Consumer<ResourceSymbol> consumer) {
    resourceNames.forEach(resourceName -> register(resourceName, consumer));
  }
}
