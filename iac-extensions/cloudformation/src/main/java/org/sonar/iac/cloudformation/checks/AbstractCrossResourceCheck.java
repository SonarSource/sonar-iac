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
package org.sonar.iac.cloudformation.checks;

import java.util.HashMap;
import java.util.Map;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.yaml.tree.FileTree;

public abstract class AbstractCrossResourceCheck extends AbstractResourceCheck {

  protected Map<String, Resource> resourceNameToResource = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (CheckContext ctx, FileTree tree) -> {
      var fileResources = getFileResources(tree);
      resourceNameToResource.clear();
      fileResources.forEach(r -> resourceNameToResource.put(r.name().value(), r));
      fileResources.forEach(r -> checkResource(ctx, r));
    });
  }

}
