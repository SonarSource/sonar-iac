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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.yaml.tree.ScalarTree;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck implements IacCheck {
  private static final String MESSAGE = "Make sure that using clear-text protocols is safe here.";
  private static final List<String> INSECURE_PROTOCOLS = List.of("http", "ftp");
  private static final Pattern LOOPBACK_PATTERN = Pattern.compile("localhost|127(?:\\.\\d+){0,2}\\.\\d+$|^(?:0*:)*+:?0*1", Pattern.CASE_INSENSITIVE);

  @Override
  public void initialize(InitContext init) {
    init.register(ScalarTree.class, ClearTextProtocolsCheck::checkUrlsInScalar);
  }

  private static void checkUrlsInScalar(CheckContext ctx, ScalarTree scalarTree) {
    var scalar = scalarTree.value();
    try {
      var url = new URL(scalar);
      var scheme = INSECURE_PROTOCOLS.stream().filter(url.getProtocol()::equals).findFirst();
      if (scheme.isPresent()) {
        var domain = url.getHost();
        if (!LOOPBACK_PATTERN.matcher(domain).find()) {
          ctx.reportIssue(scalarTree, MESSAGE);
        }
      }
    } catch (IOException e) {
      // not a valid URL, ignore
    }
  }
}
