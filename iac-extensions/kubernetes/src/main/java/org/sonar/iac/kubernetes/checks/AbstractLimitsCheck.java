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
package org.sonar.iac.kubernetes.checks;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.object.BlockObject;

import static org.sonar.iac.common.yaml.TreePredicates.isSet;

public abstract class AbstractLimitsCheck extends AbstractKubernetesObjectCheck {
  private static final String KIND_POD = "Pod";
  private static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");

  @Override
  void registerObjectCheck() {
    register(KIND_POD, (BlockObject pod) -> {
      Stream<BlockObject> containers = pod.blocks("containers");
      reportMissingLimit(containers);
    });

    register(KIND_WITH_TEMPLATE, (BlockObject obj) -> {
      Stream<BlockObject> containers = obj.block("template").block("spec").blocks("containers");
      reportMissingLimit(containers);
    });
  }

  private void reportMissingLimit(Stream<BlockObject> containers) {
    containers.forEach(container -> container.block("resources")
      .block("limits")
      .attribute(getLimitAttributeKey())
      .reportIfAbsent(getFirstChildElement(container), getMessage())
      .reportIfValue(isSet().negate(), getMessage()));
  }

  @Nullable
  static HasTextRange getFirstChildElement(BlockObject blockObject) {
    if (blockObject.tree != null) {
      return blockObject.tree.elements().get(0).key().metadata();
    }
    return null;
  }

  abstract String getLimitAttributeKey();

  abstract String getMessage();
}