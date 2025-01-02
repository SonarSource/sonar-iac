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

import static org.sonar.iac.common.yaml.TreePredicates.isEqualTo;

@Rule(key = "S6429")
public class DockerSocketCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Make sure exposing the Docker socket is safe here.";
  // Here the path to the docker socket is immutable, thus it must be hardcoded. Therefore, we remove the warning about the hardcoded URIs.
  @SuppressWarnings("java:S1075")
  private static final String DOCKER_SOCK_PATH = "/var/run/docker.sock";
  private static final String HOST_PATH = "hostPath";

  @Override
  protected void registerObjectCheck() {
    register("Pod", pod -> pod.blocks("volumes").forEach(container -> container.block(HOST_PATH)
      .attribute("path")
      .reportIfValue(isEqualTo(DOCKER_SOCK_PATH), MESSAGE)));

    register("PersistentVolume", perVolu -> perVolu.block(HOST_PATH)
      .attribute("path")
      .reportIfValue(isEqualTo(DOCKER_SOCK_PATH), MESSAGE));

    register(List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet"),
      obj -> obj.block("template").block("spec").blocks("volumes").forEach(container -> container.block(HOST_PATH)
        .attribute("path")
        .reportIfValue(isEqualTo(DOCKER_SOCK_PATH), MESSAGE)));
  }
}
