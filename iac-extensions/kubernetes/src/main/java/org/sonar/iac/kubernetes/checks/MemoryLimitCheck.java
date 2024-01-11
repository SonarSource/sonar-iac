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
import org.sonar.check.Rule;

import static org.sonar.iac.common.yaml.TreePredicates.isSet;

@Rule(key = "S6864")
public class MemoryLimitCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Make sure it is safe not to set memory limit.";

  @Override
  void registerObjectCheck() {
    register("Pod", pod -> pod.blocks("containers").forEach(container -> container.block("resources")
      .block("limits")
      .attribute("memory")
      .reportIfValue(isSet().negate(), MESSAGE)));

    register(List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob"),
      obj -> obj.block("template").block("spec").blocks("containers").forEach(container -> container.block("resources")
        .block("limits")
        .attribute("memory")
        .reportIfValue(isSet().negate(), MESSAGE)));
  }
}
