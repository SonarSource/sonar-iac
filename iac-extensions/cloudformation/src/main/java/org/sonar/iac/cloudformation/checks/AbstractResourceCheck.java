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
package org.sonar.iac.cloudformation.checks;

import javax.annotation.Nullable;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

public abstract class AbstractResourceCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> {
      if (!(tree.root() instanceof MappingTree)) {
        return;
      }

      CloudformationTree resourcesTree = MappingTreeUtils.getValue((MappingTree) tree.root(), "Resources").orElse(null);
      if (!(resourcesTree instanceof MappingTree)) {
        return;
      }

      ((MappingTree) resourcesTree).elements().stream()
        .filter(element -> element.value() instanceof MappingTree)
        .map(element -> Resource.fromMapping((MappingTree) element.value()))
        .forEach(r -> checkResource(ctx, r));
    });
  }

  protected abstract void checkResource(CheckContext ctx, Resource resource);

  public static class Resource {
    private final CloudformationTree type;
    private final CloudformationTree properties;

    private Resource(@Nullable CloudformationTree type, @Nullable CloudformationTree properties) {
      this.type = type;
      this.properties = properties;
    }

    private static Resource fromMapping(MappingTree mapping) {
      return new Resource(MappingTreeUtils.getValue(mapping, "Type").orElse(null), MappingTreeUtils.getValue(mapping, "Properties").orElse(null));
    }

    public CloudformationTree type() {
      return type;
    }

    public CloudformationTree properties() {
      return properties;
    }
  }
}
