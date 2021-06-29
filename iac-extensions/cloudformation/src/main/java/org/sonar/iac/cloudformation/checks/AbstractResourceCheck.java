/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import javax.annotation.Nullable;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.cloudformation.checks.utils.ScalarTreeUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

public abstract class AbstractResourceCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> {
      CloudformationTree resourcesTree = MappingTreeUtils.getValue(tree.root(), "Resources").orElse(null);
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

    Resource(@Nullable CloudformationTree type, @Nullable CloudformationTree properties) {
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

    public boolean isType(String expectedType) {
      return ScalarTreeUtils.getValue(type).orElse("null").equalsIgnoreCase(expectedType);
    }
  }

  static boolean isS3Bucket(Resource resource) {
    return resource.isType("AWS::S3::Bucket");
  }
}
