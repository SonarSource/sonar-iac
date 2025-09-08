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
package org.sonar.iac.kubernetes.checks;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6596")
public class SpecificVersionTagCheck extends AbstractKubernetesObjectCheck {
  private static final String MESSAGE = "Use a specific version tag for the image.";
  protected static final String KIND_POD = "Pod";
  protected static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  private static final Predicate<YamlTree> SENSITIVE_VERSION_TAG_PREDICATE = tree -> TextUtils.matchesValue(tree, SpecificVersionTagCheck::hasSensitiveVersionTag).isTrue();

  @Override
  protected void registerObjectCheck() {
    register(KIND_POD, document -> checkDocument(document, false));
    register(KIND_WITH_TEMPLATE, document -> checkDocument(document, true));
  }

  private static void checkDocument(BlockObject document, boolean isKindWithTemplate) {
    Stream<BlockObject> containers;

    if (isKindWithTemplate) {
      containers = document.block("template").block("spec").blocks("containers");
    } else {
      containers = document.blocks("containers");
    }
    containers
      .map(container -> container.attribute("image"))
      .forEach(image -> image.reportIfValue(SENSITIVE_VERSION_TAG_PREDICATE, MESSAGE));
  }

  private static boolean hasSensitiveVersionTag(String fullImageName) {
    if (fullImageName.isBlank() || fullImageName.contains("@")) {
      // image name is empty, unresolved with Helm, or using digest: do not raise an issue
      return false;
    }
    if (fullImageName.contains(":")) {
      // raise an issue if the version tag is "latest"
      String[] splitImageName = fullImageName.split(":");
      return splitImageName.length > 1 && "latest".equals(splitImageName[1]);
    }
    // no version tag specified, kubernetes assumes "latest"
    return !fullImageName.startsWith("$");
  }
}
