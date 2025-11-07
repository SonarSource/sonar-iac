/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.object.AttributeObject;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6473")
public class ExposedAdministrationServicesCheck extends AbstractKubernetesObjectCheck {

  private static final Logger LOG = LoggerFactory.getLogger(ExposedAdministrationServicesCheck.class);
  private static final String MESSAGE = "Make sure that exposing administration services is safe here.";
  private static final String KIND_POD = "Pod";
  private static final String KIND_SERVICE = "Service";
  private static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  private static final String DEFAULT_SENSITIVE_PORTS = "22, 23, 3389, 5800, 5900";
  private static final Pattern PORTS_SPLIT_PATTERN = Pattern.compile(",\\s*+");

  private List<String> sensitivePorts;

  @RuleProperty(
    key = "ports",
    description = "Comma separated list of sensitive ports.",
    defaultValue = DEFAULT_SENSITIVE_PORTS)
  public String portList = DEFAULT_SENSITIVE_PORTS;

  public ExposedAdministrationServicesCheck() {
    this.sensitivePorts = null;
  }

  public ExposedAdministrationServicesCheck(List<String> sensitivePorts) {
    this.sensitivePorts = List.copyOf(sensitivePorts);
  }

  @Override
  protected void registerObjectCheck() {
    if (sensitivePorts == null) {
      sensitivePorts = parseSensitivePorts(portList);
    }

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

  private static List<String> parseSensitivePorts(String ports) {
    try {
      String[] parsedPorts = PORTS_SPLIT_PATTERN.split(ports.strip());
      // Check that all ports are integers
      Arrays.stream(parsedPorts).forEach(Integer::parseInt);
      return Arrays.asList(parsedPorts);
    } catch (NumberFormatException e) {
      LOG.warn("The port list provided for ExposedAdministrationServicesCheck (S6473) is not a comma seperated list of integers. " +
        "The default list is used. Invalid list of ports \"{}\"", ports);
      return parseSensitivePorts(DEFAULT_SENSITIVE_PORTS);
    }
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
