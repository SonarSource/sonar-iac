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
package org.sonar.iac.docker.checks;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.AddInstruction;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.CommandInstruction;

import static org.sonar.iac.docker.tree.api.DockerTree.Kind.ADD;
import static org.sonar.iac.docker.tree.api.DockerTree.Kind.CMD;
import static org.sonar.iac.docker.tree.api.DockerTree.Kind.ENTRYPOINT;
import static org.sonar.iac.docker.tree.api.DockerTree.Kind.RUN;

@Rule(key = "S5332")
public class UnencryptedProtocolCheck implements IacCheck {

  private static final Pattern UNENCRYPTED_PROTOCOLS = Pattern.compile("(http|ftp)://(?<rest>.+)", Pattern.CASE_INSENSITIVE);

  private static final String LOOPBACK_IPV4 = "^127(?:\\.\\d+){2}\\.\\d+";
  private static final String LOOPBACK_IPV6 = "^(?:0*:){7}:?0*1|^::1";
  private static final Pattern LOOPBACK = Pattern.compile("^localhost|" + LOOPBACK_IPV4 + "|" + LOOPBACK_IPV6, Pattern.CASE_INSENSITIVE);
  private static final String MESSAGE = "Make sure that using clear-text protocols is safe here.";

  @Override
  public void initialize(InitContext init) {
    init.register(CommandInstruction.class, (ctx, commandInstruction) -> {
      if (!commandInstruction.is(ADD, ENTRYPOINT, CMD, RUN))
        return;
      checkUnencryptedProtocols(ctx, commandInstruction.arguments());
    });

    init.register(AddInstruction.class, (ctx, add) -> {
      checkUnencryptedProtocols(ctx, add.srcs());
      checkUnencryptedProtocols(ctx, List.of(add.dest()));
    });
  }

  private static void checkUnencryptedProtocols(CheckContext ctx, List<Argument> paths) {
    for (Argument path : paths) {
      String resolvedPath = ArgumentResolution.of(path).value();
      if (resolvedPath != null) {
        Matcher matcher = UNENCRYPTED_PROTOCOLS.matcher(resolvedPath);
        if (matcher.find() && !LOOPBACK.matcher(matcher.group("rest")).find()) {
          ctx.reportIssue(path, MESSAGE);
        }
      }
    }
  }
}
