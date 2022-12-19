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
package org.sonar.iac.docker.checks;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.ExposeTree;
import org.sonar.iac.docker.tree.api.PortTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ExposePortCheck implements IacCheck {

  private static final String MESSAGE = "Make sure that exposing administration services is safe here.";

  private static final List<Integer> DEFAULT_SENSITIVE_PORTS = List.of(22, 23, 3389, 5800, 5900);

  @Override
  public void initialize(InitContext init) {
    init.register(ExposeTree.class, (ctx, instruction) -> instruction.ports().forEach(port -> checkPort(ctx, port)));
  }

  private void checkPort(CheckContext ctx, PortTree port) {
    if (isTcpProtocol(port.protocol())) {
      try {
        int min = Integer.parseInt(port.portMin().value());
        int max = Integer.parseInt(port.portMax().value());
        if (isSensitivePort(min, max)) {
          ctx.reportIssue(port, MESSAGE);
        }
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
  }

  private static boolean isSensitivePort(int min, int max) {
    return DEFAULT_SENSITIVE_PORTS.stream().anyMatch(sensitivePort -> isBetween(sensitivePort, min, max));
  }

  private static boolean isBetween(int value, int min, int max) {
    return value >= min && value <= max;
  }

  private static boolean isTcpProtocol(@Nullable SyntaxToken protocol) {
    if (protocol != null) {
      return "tcp".equals(protocol.value());
    }
    return true;
  }

}
