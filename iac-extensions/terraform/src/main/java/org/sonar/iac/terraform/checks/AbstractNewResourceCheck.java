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
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.CheckForNull;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

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

  protected void register(Collection<String> resourceNames, Consumer<ResourceSymbol> consumer) {
    resourceNames.forEach(resourceName -> register(resourceName, consumer));
  }

  /** If needed - add similar method isData(BlockTree blockTree) */
  public static boolean isResource(BlockTree blockTree) {
    return "resource".equals(blockTree.key().value());
  }

  /** If needed - add similar method isResourceOfType(BlockTree blockTree, String dataType) */
  public static boolean isDataOfType(BlockTree blockTree, String dataType) {
    return "data".equals(blockTree.key().value()) && dataType.equals(resourceType(blockTree));
  }

  /** Despite its name, this method works fine for 'resource', 'data' and all other sorts of Terraform top-level blocks */
  @CheckForNull
  public static String resourceType(BlockTree tree) {
    return tree.labels().isEmpty() ? null : tree.labels().get(0).value();
  }
}
