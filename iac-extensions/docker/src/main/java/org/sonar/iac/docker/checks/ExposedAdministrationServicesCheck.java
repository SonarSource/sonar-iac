/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.docker.checks;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ExposeInstruction;

@Rule(key = "S6473")
public class ExposedAdministrationServicesCheck implements IacCheck {

  private static final Logger LOG = LoggerFactory.getLogger(ExposedAdministrationServicesCheck.class);
  private static final String MESSAGE = "Make sure that exposing administration services is safe here.";
  private static final String DEFAULT_SENSITIVE_PORTS = "22, 23, 3389, 5800, 5900";

  private List<Integer> sensitivePorts;

  @RuleProperty(
    key = "ports",
    description = "Comma separated list of sensitive ports.",
    defaultValue = DEFAULT_SENSITIVE_PORTS)
  String portList = DEFAULT_SENSITIVE_PORTS;

  @Override
  public void initialize(InitContext init) {
    init.register(ExposeInstruction.class, (ctx, instruction) -> instruction.arguments().forEach(arg -> checkPort(ctx, arg)));
    sensitivePorts = sensitivePorts(portList);
  }

  private static List<Integer> sensitivePorts(String ports) {
    try {
      return Arrays.stream(ports.split(",\\s*+")).map(Integer::parseInt).toList();
    } catch (NumberFormatException e) {
      LOG.warn("The port list provided for ExposedAdministrationServicesCheck (S6473) is not a comma seperated list of integers. " +
        "The default list is used. Invalid list of ports \"{}\"", ports);
      return sensitivePorts(DEFAULT_SENSITIVE_PORTS);
    }
  }

  private void checkPort(CheckContext ctx, Argument arg) {
    String portStr = ArgumentResolution.of(arg).value();
    try {
      Port port = new Port.PortParser(portStr).parsePort().parseProtocol().build();
      if (port.protocol == Protocol.TCP && isSensitivePort(port.portMin, port.portMax)) {
        ctx.reportIssue(arg, MESSAGE);
      }
    } catch (NumberFormatException e) {
      // do nothing
    }
  }

  private boolean isSensitivePort(int min, int max) {
    return sensitivePorts.stream().anyMatch(sensitivePort -> isBetween(sensitivePort, min, max));
  }

  private static boolean isBetween(int value, int min, int max) {
    return value >= min && value <= max;
  }

  enum Protocol {
    TCP, UDP
  }

  /**
   * Represent a single Port exposition in docker, with a range and a protocol.
   */
  static class Port {
    int portMin;
    int portMax;
    Protocol protocol;

    /**
     * Parse a string as a Port representation.
     * Expected format : [0-9]+(-[0-9]+)?(/(tcp|udp)?)?
     */
    static class PortParser {
      String[] splittedProtocol;
      Port port;

      public PortParser(String str) {
        port = new Port();
        splittedProtocol = str.split("/");
      }

      public PortParser parsePort() {
        String[] ports = splittedProtocol[0].split("-");
        port.portMin = Integer.parseInt(ports[0]);
        if (ports.length > 1) {
          port.portMax = Integer.parseInt(ports[1]);
        } else {
          port.portMax = port.portMin;
        }
        return this;
      }

      public PortParser parseProtocol() {
        if (splittedProtocol.length > 1 && splittedProtocol[1].equalsIgnoreCase("udp")) {
          port.protocol = Protocol.UDP;
        } else {
          port.protocol = Protocol.TCP;
        }
        return this;
      }

      public Port build() {
        return port;
      }
    }
  }

}
