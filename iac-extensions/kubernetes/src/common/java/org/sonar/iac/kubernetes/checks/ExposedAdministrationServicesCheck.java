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
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.object.AttributeObject;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6473")
public class ExposedAdministrationServicesCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Make sure that exposing administration services is safe here.";
  private static final String KIND_POD = "Pod";
  private static final String KIND_SERVICE = "Service";
  private static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  public static final List<String> DEFAULT_SENSITIVE_PORTS = List.of("22", "23", "3389", "5800", "5900");
  private final List<String> sensitivePorts;

  public ExposedAdministrationServicesCheck() {
    this(DEFAULT_SENSITIVE_PORTS);
  }

  public ExposedAdministrationServicesCheck(List<String> sensitivePorts) {
    this.sensitivePorts = List.copyOf(sensitivePorts);
  }

  @Override
  protected void registerObjectCheck() {
    register(KIND_POD,
      pod -> pod.blocks("containers")
        .forEach(this::reportOnSensitiveContainerPorts));

    register(KIND_WITH_TEMPLATE,
      obj -> obj.block("template")
        .block("spec")
        .blocks("containers")
        .forEach(this::reportOnSensitiveContainerPorts));

    register(KIND_SERVICE, this::reportOnSensitiveServicePorts);
  }

  private void reportOnSensitiveContainerPorts(BlockObject container) {
    container.blocks("ports")
      .filter(ExposedAdministrationServicesCheck::isProtocolTcp)
      .forEach(port -> reportIfSensitivePort(port.attribute("containerPort"), port.attribute("hostPort")));
  }

  private void reportOnSensitiveServicePorts(BlockObject service) {
    service.blocks("ports")
      .filter(ExposedAdministrationServicesCheck::isProtocolTcp)
      .forEach(port -> reportIfSensitivePort(port.attribute("targetPort"), port.attribute("port")));
  }

  private static boolean isProtocolTcp(BlockObject port) {
    AttributeObject protocol = port.attribute("protocol");
    if (protocol.isAbsent()) {
      return true;
    }
    return protocol.isValue(tree -> TextUtils.isValue(tree, "TCP").isTrue());
  }

  private void reportIfSensitivePort(AttributeObject internalPort, AttributeObject externalPort) {
    boolean isInternalPortSensitive = internalPort.isValue(this::isSensitivePort);
    boolean isExternalPortSensitive = externalPort.isValue(this::isSensitivePort);
    if (isInternalPortSensitive) {
      if (isExternalPortSensitive) {
        var portLocation = new SecondaryLocation(externalPort.tree.value(), MESSAGE);
        internalPort.reportOnValue(MESSAGE, List.of(portLocation));
      } else {
        internalPort.reportOnValue(MESSAGE);
      }
    } else if (isExternalPortSensitive) {
      externalPort.reportOnValue(MESSAGE);
    }
  }

  private boolean isSensitivePort(YamlTree port) {
    return TextUtils.matchesValue(port, sensitivePorts::contains).isTrue();
  }
}
