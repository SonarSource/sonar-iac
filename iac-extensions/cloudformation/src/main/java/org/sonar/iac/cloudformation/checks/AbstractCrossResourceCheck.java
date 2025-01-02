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
      beforeCheckResource();
      fileResources.forEach(r -> checkResource(ctx, r));
    });
  }

  protected void beforeCheckResource() {
    // default empty implementation
  }
}
