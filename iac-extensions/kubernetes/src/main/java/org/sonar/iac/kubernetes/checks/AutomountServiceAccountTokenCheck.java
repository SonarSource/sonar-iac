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
import org.sonar.iac.common.yaml.object.BlockObject;

import static org.sonar.iac.common.yaml.TreePredicates.isSet;
import static org.sonar.iac.common.yaml.TreePredicates.isTrue;
import static org.sonar.iac.kubernetes.checks.AbstractLimitsCheck.getFirstChildElement;

@Rule(key = "S6865")
public class AutomountServiceAccountTokenCheck extends AbstractKubernetesObjectCheck {
  private static final String MESSAGE = "Set automountServiceAccountToken to false for this container.";
  private static final String KEY = "automountServiceAccountToken";
  private static final String KIND_POD = "Pod";
  private static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");

  @Override
  void registerObjectCheck() {
    register(KIND_POD, (BlockObject pod) -> pod.attribute(KEY)
      .reportIfAbsent(getFirstChildElement(pod), MESSAGE)
      .reportIfValue(isSet().negate(), MESSAGE)
      .reportIfValue(isTrue(), MESSAGE));

    register(KIND_WITH_TEMPLATE, (BlockObject obj) -> obj.block("template").block("spec").attribute(KEY)
      .reportIfAbsent(getFirstChildElement(obj.block("template").block("spec")), MESSAGE)
      .reportIfValue(isSet().negate(), MESSAGE)
      .reportIfValue(isTrue(), MESSAGE));
  }
}
