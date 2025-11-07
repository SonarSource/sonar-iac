/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.List;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public abstract class AbstractResourceCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> getFileResources(tree).forEach(r -> checkResource(ctx, r)));
  }

  public static List<Resource> getFileResources(FileTree file) {
    return file.documents().stream()
      .flatMap(document -> PropertyUtils.value(document, "Resources", MappingTree.class).stream())
      .flatMap(resources -> resources.elements().stream())
      .filter(resource -> resource.key() instanceof ScalarTree && resource.value() instanceof MappingTree)
      .map(resource -> Resource.fromMapping((ScalarTree) resource.key(), (MappingTree) resource.value()))
      .filter(Objects::nonNull)
      .toList();
  }

  protected abstract void checkResource(CheckContext ctx, Resource resource);

  public static class Resource {
    private final ScalarTree name;
    private final YamlTree type;
    private final YamlTree properties;

    Resource(ScalarTree name, YamlTree type, @Nullable YamlTree properties) {
      this.name = name;
      this.type = type;
      this.properties = properties;
    }

    @CheckForNull
    private static Resource fromMapping(ScalarTree name, MappingTree mapping) {
      return PropertyUtils.value(mapping, "Type", YamlTree.class).map(typeTree -> new Resource(
        name,
        typeTree,
        PropertyUtils.valueOrNull(mapping, "Properties", YamlTree.class))).orElse(null);

    }

    public ScalarTree name() {
      return name;
    }

    public YamlTree type() {
      return type;
    }

    @CheckForNull
    public YamlTree properties() {
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
