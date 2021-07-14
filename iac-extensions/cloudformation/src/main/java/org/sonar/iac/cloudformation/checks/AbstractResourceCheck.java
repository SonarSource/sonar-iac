/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import org.sonar.iac.common.checks.AttributeUtils;
import org.sonar.iac.common.checks.TextUtils;

public abstract class AbstractResourceCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> getFileResources(tree).forEach(r -> checkResource(ctx, r)));
  }

  public static List<Resource> getFileResources(FileTree file) {
    Tree resourcesTree = AttributeUtils.valueOrNull(file.root(), "Resources");
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
      Optional<CloudformationTree> typeTree = AttributeUtils.value(mapping, "Type", CloudformationTree.class);
      if (!typeTree.isPresent()) {
        return null;
      }

      return new Resource(
        name,
        typeTree.get(),
        AttributeUtils.valueOrNull(mapping, "Properties", CloudformationTree.class)
      );
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
}
