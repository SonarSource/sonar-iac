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
import org.sonar.check.Rule;

import static org.sonar.iac.common.yaml.TreePredicates.isTrue;

@Rule(key = "S6428")
public class ContainerPrivilegedModeCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Ensure that enabling privileged mode is safe here.";

  @Override
  protected void registerObjectCheck() {
    checkOnPrivilegedModeWithKey("containers");
    checkOnPrivilegedModeWithKey("initContainers");
  }

  private void checkOnPrivilegedModeWithKey(String key) {
    register("Pod", pod -> pod.blocks(key).forEach(container -> container.block("securityContext")
      .attribute("privileged")
      .reportIfValue(isTrue(), MESSAGE)));

    register(List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet"),
      obj -> obj.block("template").block("spec").blocks(key).forEach(container -> container.block("securityContext")
        .attribute("privileged")
        .reportIfValue(isTrue(), MESSAGE)));
  }
}
