/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import static org.sonar.iac.common.yaml.TreePredicates.isEqualTo;

@Rule(key = "S6429")
public class DockerSocketCheck extends KubernetesObjectCheck {

  private static final String MESSAGE = "Make sure exposing the Docker socket is safe here.";
  @SuppressWarnings("java:S1075")
  private static final String DOCKER_SOCK_PATH = "/var/run/docker.sock";
  private static final String HOST_PATH = "hostPath";

  @Override
  void registerObjectCheck() {
    register("Pod", pod ->
      pod.blocks("volumes").forEach(container ->
        container.block(HOST_PATH)
          .attribute("path")
            .reportIfValue(isEqualTo(DOCKER_SOCK_PATH), MESSAGE)
      )
    );

    register("PersistentVolume", perVolu ->
      perVolu.block(HOST_PATH)
          .attribute("path")
            .reportIfValue(isEqualTo(DOCKER_SOCK_PATH), MESSAGE)
    );

    register(List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet"), obj ->
      obj.block("template").block("spec").blocks("volumes").forEach(container ->
        container.block(HOST_PATH)
          .attribute("path")
            .reportIfValue(isEqualTo(DOCKER_SOCK_PATH), MESSAGE)
      )
    );
  }
}
