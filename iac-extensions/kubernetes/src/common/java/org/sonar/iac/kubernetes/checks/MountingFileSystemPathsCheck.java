/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import static org.sonar.iac.common.yaml.TreePredicates.startsWith;

@Rule(key = "S6433")
public class MountingFileSystemPathsCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Make sure mounting the file system path is safe here.";
  private static final List<String> SENSITIVE_PATHS = List.of("/bin", "/boot", "/etc", "/home", "/root", "/sbin", "/usr", "/var");
  private static final String HOST_PATH = "hostPath";

  @Override
  protected void registerObjectCheck() {
    register("Pod", pod -> pod.blocks("volumes").forEach(container -> container.block(HOST_PATH)
      .attribute("path")
      .reportIfValue(startsWith(SENSITIVE_PATHS), MESSAGE)));

    register("PersistentVolume", perVolu -> perVolu.block(HOST_PATH)
      .attribute("path")
      .reportIfValue(startsWith(SENSITIVE_PATHS), MESSAGE));

    register(List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet"),
      obj -> obj.block("template").block("spec").blocks("volumes").forEach(container -> container.block(HOST_PATH)
        .attribute("path")
        .reportIfValue(startsWith(SENSITIVE_PATHS), MESSAGE)));
  }
}
