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
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.TreePredicates;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6473")
public class ExposedAdministrationServicesCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Make sure that exposing administration services is safe here.";
  private static final String KIND_POD = "Pod";
  private static final String KIND_SERVICE = "Service";
  private static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  private static final List<String> SENSITIVE_PORTS = List.of("22", "23", "3389", "5800", "5900");

  @Override
  void registerObjectCheck() {
    register(KIND_POD, pod -> pod.blocks("containers").forEach(container -> reportOnSensitivePorts(container, "containerPort")));

    register(KIND_WITH_TEMPLATE, obj -> obj.block("template").block("spec").blocks("containers").forEach(container -> reportOnSensitivePorts(container, "containerPort")));

    register(KIND_SERVICE, (BlockObject service) -> {
      if (isLoadBalancerService(service)) {
        reportOnSensitivePorts(service, "targetPort");
      }
    });
  }

  private static void reportOnSensitivePorts(BlockObject container, String sensitiveKey) {
    container.blocks("ports").forEach(port -> port.attribute(sensitiveKey).reportIfValue(isSensitivePort(), MESSAGE));
  }

  private static Predicate<YamlTree> isSensitivePort() {
    return t -> TextUtils.matchesValue(t, SENSITIVE_PORTS::contains).isTrue();
  }

  private static boolean isLoadBalancerService(BlockObject service) {
    TupleTree typeTree = service.attribute("type").tree;
    if (typeTree != null) {
      return TreePredicates.isEqualTo("LoadBalancer").test(typeTree.value());
    }
    return false;
  }
}
