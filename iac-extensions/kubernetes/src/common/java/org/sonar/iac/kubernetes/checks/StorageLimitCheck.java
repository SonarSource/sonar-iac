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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.yaml.object.BlockObject;

import static org.sonar.iac.common.yaml.TreePredicates.isEqualTo;
import static org.sonar.iac.common.yaml.TreePredicates.isTrue;

@Rule(key = "S6870")
public class StorageLimitCheck extends AbstractLimitCheck {
  private static final String MESSAGE = "Specify a storage limit for this container.";
  private static final String MESSAGE_SECONDARY = "for this volume mount";
  private static final String RESOURCE_NAME = "ephemeral-storage";

  private final Set<String> excludedVolumeNames = new HashSet<>();
  private List<BlockObject> sensitiveVolumeMounts = new ArrayList<>();

  @Override
  String getResourceName() {
    return RESOURCE_NAME;
  }

  @Override
  String getMessage() {
    return MESSAGE;
  }

  @Override
  protected void onEachSpec(BlockObject spec) {
    excludedVolumeNames.clear();
    spec.blocks("volumes").forEach((BlockObject volume) -> {
      var name = volume.attribute("name").asStringValue();
      if (name != null && shouldVolumeBeExcluded(volume)) {
        excludedVolumeNames.add(name);
      }
    });
  }

  private static boolean shouldVolumeBeExcluded(BlockObject volume) {
    return volume.attribute("configMap").isPresent()
      || volume.attribute("secret").isPresent()
      || volume.childrenBlocks().anyMatch(block -> block.attribute("readOnly").isValue(isTrue()));
  }

  @Override
  protected boolean shouldInvestigateContainer(BlockObject container) {
    sensitiveVolumeMounts = container.blocks("volumeMounts")
      .filter(StorageLimitCheck::isNotReadOnly)
      .filter(this::isNotExcludedVolume)
      .toList();

    return !sensitiveVolumeMounts.isEmpty();
  }

  private static boolean isNotReadOnly(BlockObject volumeMount) {
    return !(volumeMount.attribute("readOnly").isValue(isTrue())
      || volumeMount.attribute("recursiveReadOnly").isValue(isEqualTo("Enabled")));
  }

  private boolean isNotExcludedVolume(BlockObject volumeMount) {
    return !excludedVolumeNames.contains(volumeMount.attribute("name").asStringValue());
  }

  @Override
  protected List<SecondaryLocation> extraSecondaryLocations() {
    return sensitiveVolumeMounts.stream()
      .map(AbstractResourceManagementCheck::getFirstChildElement)
      .filter(Objects::nonNull)
      .map(volumeMountNameTree -> new SecondaryLocation(volumeMountNameTree, MESSAGE_SECONDARY))
      .toList();
  }
}
