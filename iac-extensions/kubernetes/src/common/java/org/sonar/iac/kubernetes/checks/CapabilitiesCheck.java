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

@Rule(key = "S5849")
public class CapabilitiesCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Make sure setting capabilities is safe here.";

  @Override
  void registerObjectCheck() {
    register("Pod", pod -> pod.blocks("containers").forEach(container -> container.block("securityContext")
      .block("capabilities")
      .list("add")
      .reportIfAnyItem(isSet(), MESSAGE)));

    register(List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet"),
      obj -> obj.block("template").block("spec").blocks("containers").forEach(container -> container.block("securityContext")
        .block("capabilities")
        .list("add")
        .reportIfAnyItem(isSet(), MESSAGE)));
  }
}
