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

@Rule(key = "S6430")
public class PrivilegeEscalationCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Make sure that enabling privilege escalation is safe here.";
  private static final String PRIVILEGE_ESCALATION_ATTRIBUTE = "allowPrivilegeEscalation";

  @Override
  protected void registerObjectCheck() {
    register("Pod", pod -> pod.blocks("containers").forEach(container -> container.block("securityContext")
      .attribute(PRIVILEGE_ESCALATION_ATTRIBUTE)
      .reportIfValue(isTrue(), MESSAGE)));

    register(List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet"),
      obj -> obj.block("template").block("spec").blocks("containers").forEach(container -> container.block("securityContext")
        .attribute(PRIVILEGE_ESCALATION_ATTRIBUTE)
        .reportIfValue(isTrue(), MESSAGE)));
  }
}
