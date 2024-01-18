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
import java.util.Optional;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.yaml.object.BlockObject;

import static org.sonar.iac.common.yaml.TreePredicates.isSet;

@Rule(key = "S6864")
public class MemoryLimitCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Specify a memory limit for this container.";
  private static final String KIND_POD = "Pod";
  private static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");

  private static void accept(BlockObject blockObject) {
    blockObject.blocks("containers").forEach(container -> container.block("resources")
      .block("limits")
      .attribute("memory")
      .reportIfValue(isSet().negate(), MESSAGE));
  }

  @Override
  void registerObjectCheck() {
    memoryCheck();
    limitsCheck();
    resourcesCheck();

    // emptyCheck
    register(KIND_POD, MemoryLimitCheck::accept);
    register(KIND_WITH_TEMPLATE, obj -> accept(obj.block("template").block("spec")));
  }

  private void memoryCheck() {
    register(KIND_POD, pod -> {
      Stream<BlockObject> containers = pod.blocks("containers");
      missingMemory(pod, containers);
    });

    register(KIND_WITH_TEMPLATE, obj -> {
      Stream<BlockObject> containers = obj.block("template").block("spec").blocks("containers");
      missingMemory(obj.block("template").block("spec"), containers);
    });
  }

  private void missingMemory(BlockObject pod, Stream<BlockObject> containers) {
    Optional<BlockObject> containerBlock = containers.filter(container -> container.block("resources").block("limits").attribute("memory").isAbsent())
      .findFirst();
    if (containerBlock.isPresent()) {
      assert pod.tree != null;
      TextRange textRange = pod.tree.elements().get(0).key().metadata().textRange();
      pod.ctx.reportIssue(textRange, MESSAGE);
    }
  }

  private void limitsCheck() {
    register(KIND_POD, pod -> {
      Stream<BlockObject> containers = pod.blocks("containers");
      missingLimits(pod, containers);
    });

    register(KIND_WITH_TEMPLATE, obj -> {
      Stream<BlockObject> containers = obj.block("template").block("spec").blocks("containers");
      missingLimits(obj.block("template").block("spec"), containers);
    });
  }

  private void missingLimits(BlockObject pod, Stream<BlockObject> containers) {
    Optional<BlockObject> containerBlock = containers.filter(container -> container.block("resources").block("limits").isAbsent())
      .findFirst();
    if (containerBlock.isPresent()) {
      assert pod.tree != null;
      TextRange textRange = pod.tree.elements().get(0).key().metadata().textRange();
      pod.ctx.reportIssue(textRange, MESSAGE);
    }
  }

  private void resourcesCheck() {
    register(KIND_POD, pod -> {
      Stream<BlockObject> containers = pod.blocks("containers");
      missingResources(pod, containers);
    });

    register(KIND_WITH_TEMPLATE, obj -> {
      Stream<BlockObject> containers = obj.block("template").block("spec").blocks("containers");
      missingResources(obj.block("template").block("spec"), containers);
    });
  }

  private void missingResources(BlockObject obj, Stream<BlockObject> containers) {
    Optional<BlockObject> containerBlock = containers.filter(container -> container.block("resources").isAbsent()).findFirst();
    if (containerBlock.isPresent()) {
      assert obj.tree != null;
      TextRange textRange = obj.tree.elements().get(0).key().metadata().textRange();
      obj.ctx.reportIssue(textRange, MESSAGE);
    }
  }
}
