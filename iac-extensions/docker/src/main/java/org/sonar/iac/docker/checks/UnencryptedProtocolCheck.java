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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.AddTree;
import org.sonar.iac.docker.tree.api.CommandInstructionTree;
import org.sonar.iac.docker.tree.api.LiteralListTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

@Rule(key = "S5332")
public class UnencryptedProtocolCheck implements IacCheck {

  private static final Pattern UNENCRYPTED_PROTOCOLS = Pattern.compile("(http|ftp)://(?<rest>.+)", Pattern.CASE_INSENSITIVE);

  private static final String LOOPBACK_IPV4 = "^127(?:\\.\\d+){0,2}\\.\\d+";
  private static final String LOOPBACK_IPV6 = "^(?:0*:){0,7}?:?0*1";
  private static final Pattern LOOPBACK = Pattern.compile("^localhost|" + LOOPBACK_IPV4 + "|" + LOOPBACK_IPV6, Pattern.CASE_INSENSITIVE);
  private static final String MESSAGE = "Make sure that using clear-text protocols is safe here.";

  @Override
  public void initialize(InitContext init) {
    init.register(CommandInstructionTree.class, (ctx, commandInstruction) -> {
      LiteralListTree arguments = commandInstruction.arguments();
      if (arguments == null) return;
      checkUnencryptedProtocols(ctx, arguments.literals());
    });

    init.register(AddTree.class, (ctx, add) -> {
      checkUnencryptedProtocols(ctx, add.srcs());
      checkUnencryptedProtocols(ctx, List.of(add.dest()));
    });
  }

  private static void checkUnencryptedProtocols(CheckContext ctx, List<SyntaxToken> syntaxTokens) {
    for (SyntaxToken syntaxToken : syntaxTokens) {
      Matcher matcher = UNENCRYPTED_PROTOCOLS.matcher(syntaxToken.value());
      if (matcher.find() && !LOOPBACK.matcher(matcher.group("rest")).find()) {
        ctx.reportIssue(syntaxToken, MESSAGE);
      }
    }
  }
}
