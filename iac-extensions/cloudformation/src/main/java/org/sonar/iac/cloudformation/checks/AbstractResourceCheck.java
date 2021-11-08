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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

public abstract class AbstractResourceCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> getFileResources(tree).forEach(r -> checkResource(ctx, r)));
  }

  public static List<Resource> getFileResources(FileTree file) {
    Tree resourcesTree = PropertyUtils.valueOrNull(file.root(), "Resources");
    if (!(resourcesTree instanceof MappingTree)) {
      return Collections.emptyList();
    }

    return ((MappingTree) resourcesTree).elements().stream()
      .filter(element -> element.key() instanceof ScalarTree && element.value() instanceof MappingTree)
      .map(element -> Resource.fromMapping((ScalarTree) element.key(), (MappingTree) element.value()))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  protected abstract void checkResource(CheckContext ctx, Resource resource);

  public static class Resource {
    private final ScalarTree name;
    private final CloudformationTree type;
    private final CloudformationTree properties;

    Resource(ScalarTree name, CloudformationTree type, @Nullable CloudformationTree properties) {
      this.name = name;
      this.type = type;
      this.properties = properties;
    }

    @CheckForNull
    private static Resource fromMapping(ScalarTree name, MappingTree mapping) {
      return PropertyUtils.value(mapping, "Type", CloudformationTree.class).map(typeTree -> new Resource(
        name,
        typeTree,
        PropertyUtils.valueOrNull(mapping, "Properties", CloudformationTree.class)
      )).orElse(null);

    }

    public ScalarTree name() {
      return name;
    }

    public CloudformationTree type() {
      return type;
    }

    @CheckForNull
    public CloudformationTree properties() {
      return properties;
    }

    public boolean isType(String expectedType) {
      return TextUtils.getValue(type).orElse("null").equalsIgnoreCase(expectedType);
    }
  }

  static boolean isS3Bucket(Resource resource) {
    return resource.isType("AWS::S3::Bucket");
  }

  protected static void reportResource(CheckContext ctx, Resource resource, String message) {
    ctx.reportIssue(resource.type, message);
  }
}
