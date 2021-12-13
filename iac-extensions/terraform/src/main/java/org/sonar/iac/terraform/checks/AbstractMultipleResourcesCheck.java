/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.api.tree.BlockTree;

public abstract class AbstractMultipleResourcesCheck extends AbstractResourceCheck {

  private final Map<String, List<BiConsumer<CheckContext, BlockTree>>> resourceChecks = new HashMap<>();

  protected abstract void registerChecks();

  protected void register(BiConsumer<CheckContext, BlockTree> resourceCheck, String... resourceNames) {
    Arrays.asList(resourceNames).forEach(resourceName ->
      resourceChecks.computeIfAbsent(resourceName, i -> new ArrayList<>()).add(resourceCheck));
  }

  @Override
  public void initialize(InitContext init) {
    super.initialize(init);
    registerChecks();
  }

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    String resourceType = getResourceType(resource);
    if (resourceChecks.containsKey(resourceType)) {
      resourceChecks.get(resourceType).forEach(consumer -> consumer.accept(ctx, resource));
    }
  }
}
