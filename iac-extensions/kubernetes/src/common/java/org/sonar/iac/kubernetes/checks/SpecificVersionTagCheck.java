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
package org.sonar.iac.kubernetes.checks;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

@Rule(key = "S6596")
public class SpecificVersionTagCheck extends AbstractKubernetesObjectCheck {
  public static final String MESSAGE = "Use a specific version tag for the image.";
  private static final String MESSAGE_SPECIFIC_FORMAT = "Use a specific version tag for the image instead of \"%s\".";
  protected static final String KIND_POD = "Pod";
  protected static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  private static final Predicate<YamlTree> SENSITIVE_VERSION_TAG_PREDICATE = tree -> TextUtils.matchesValue(tree, SpecificVersionTagCheck::hasSensitiveVersionTag)
    .isTrue();

  @Override
  void initializeCheck(KubernetesCheckContext ctx) {
    ctx.setShouldReportSecondaryInValues(true);
  }

  @Override
  protected void registerObjectCheck() {
    register(KIND_POD, document -> checkDocument(document, false));
    register(KIND_WITH_TEMPLATE, document -> checkDocument(document, true));
  }

  private void checkDocument(BlockObject document, boolean isKindWithTemplate) {
    Stream<BlockObject> containers;

    if (isKindWithTemplate) {
      containers = document.block("template").block("spec").blocks("containers");
    } else {
      containers = document.blocks("containers");
    }
    containers
      .map(container -> container.attribute("image"))
      .filter(image -> image.isValue(sensitiveVersionTagPredicate()))
      .forEach(image -> image.reportOnValue(Optional.ofNullable(image.tree)
        .map(TupleTree::value)
        .map(SpecificVersionTagCheck::extractTag)
        .map(MESSAGE_SPECIFIC_FORMAT::formatted)
        .orElse(MESSAGE)));
  }

  public Predicate<YamlTree> sensitiveVersionTagPredicate() {
    return SENSITIVE_VERSION_TAG_PREDICATE;
  }

  public static boolean hasSensitiveVersionTag(String fullImageName) {
    var tagInfo = parseImageReference(fullImageName);
    if (!tagInfo.shouldProcess) {
      return false;
    }
    // Flag if explicit "latest" or no tag specified (implicit latest)
    return tagInfo.tag == null || "latest".equals(tagInfo.tag);
  }

  @Nullable
  private static String extractTag(YamlTree tree) {
    var imageValue = TextUtils.getValue(tree).orElse("");
    var tagInfo = parseImageReference(imageValue);
    return tagInfo.shouldProcess ? tagInfo.tag : null;
  }

  private static ImageTagInfo parseImageReference(String imageReference) {
    // image name is empty, unresolved with Helm, or using digest: do not raise an issue
    if (imageReference.isBlank() || imageReference.contains("@") || imageReference.startsWith("$")) {
      return new ImageTagInfo(false, null);
    }

    if (imageReference.contains(":")) {
      var parts = imageReference.split(":", 2);
      String tag = parts.length > 1 ? parts[1] : null;
      return new ImageTagInfo(true, tag);
    }

    // no version tag specified, kubernetes assumes "latest"
    return new ImageTagInfo(true, null);
  }

  private record ImageTagInfo(boolean shouldProcess, @Nullable String tag) {
  }
}
