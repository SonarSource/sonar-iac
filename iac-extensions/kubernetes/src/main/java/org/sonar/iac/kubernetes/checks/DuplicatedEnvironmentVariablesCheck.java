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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

@Rule(key = "S6907")
public class DuplicatedEnvironmentVariablesCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Resolve the duplication of this environment variable.";
  private static final String MESSAGE_SECONDARY_LOCATION = "Duplicate environment variable without any effect.";
  private static final List<String> KIND_WITH_TEMPLATE = List.of(
    "DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  private final List<Container> containers = new ArrayList<>();

  @Override
  void registerObjectCheck() {
    register("Pod", pod -> pod.blocks("containers").forEach(this::checkContainer));

    register(KIND_WITH_TEMPLATE,
      template -> template
        .block("template")
        .block("spec")
        .blocks("containers")
        .forEach(this::checkContainer));
  }

  private void checkContainer(BlockObject containerBlock) {
    var container = new Container(new HashMap<>());
    containers.add(container);
    containerBlock.blocks("env").forEach(env -> checkEnvironmentVariable(env, container));
  }

  private static void checkEnvironmentVariable(BlockObject env, Container container) {
    var attribute = env.attribute("name");
    if (attribute.tree != null) {
      var tree = attribute.tree.value();
      if (tree instanceof ScalarTree scalarTree) {
        var name = scalarTree.value();
        container.envs.computeIfAbsent(name, key -> new ArrayList<>()).add(scalarTree);
      }
    }
  }

  @Override
  void visitDocumentOnEnd(MappingTree documentTree, CheckContext ctx) {
    containers.forEach(container -> container.envs.entrySet().stream()
      .filter(entry -> entry.getValue().size() > 1)
      .forEach(entry -> reportIssue(ctx, entry.getValue())));
    containers.clear();
  }

  private static void reportIssue(CheckContext ctx, List<YamlTree> trees) {
    var secondaryLocations = trees.stream()
      .limit(trees.size() - 1)
      .map(t -> new SecondaryLocation(t, MESSAGE_SECONDARY_LOCATION))
      .toList();
    ctx.reportIssue(trees.get(trees.size() - 1), MESSAGE, secondaryLocations);
  }

  @Override
  void initializeCheck(KubernetesCheckContext ctx) {
    ctx.setShouldReportSecondaryInValues(true);
  }

  // Container contains a map of environment variables
  record Container(Map<String, List<YamlTree>> envs) {
  }
}
