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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.network.UrlUtils;
import org.sonar.iac.common.yaml.tree.ScalarTree;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck implements IacCheck {
  private static final String MESSAGE = "Make sure that using clear-text protocols is safe here.";
  private static final List<String> INSECURE_PROTOCOLS = List.of("http://", "ftp://");

  @Override
  public void initialize(InitContext init) {
    init.register(ScalarTree.class, ClearTextProtocolsCheck::checkUrlsInScalar);
  }

  private static void checkUrlsInScalar(CheckContext ctx, ScalarTree scalarTree) {
    var scalar = scalarTree.value();
    if (INSECURE_PROTOCOLS.stream().noneMatch(scalar.toLowerCase(Locale.ROOT)::startsWith)) {
      return;
    }
    try {
      var url = new URL(scalar);
      var domain = url.getHost();
      if (!UrlUtils.COMPLIANT_HTTP_DOMAINS.matcher(domain).find()) {
        ctx.reportIssue(scalarTree, MESSAGE);
      }
    } catch (IOException e) {
      // not a valid URL, ignore
    }
  }
}
