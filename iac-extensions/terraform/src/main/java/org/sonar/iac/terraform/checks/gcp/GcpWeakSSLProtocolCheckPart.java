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
package org.sonar.iac.terraform.checks.gcp;

import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.AttributeSymbol;

import static org.sonar.iac.terraform.checks.WeakSSLProtocolCheck.OMITTING_WEAK_SSL_MESSAGE;
import static org.sonar.iac.terraform.checks.WeakSSLProtocolCheck.WEAK_SSL_MESSAGE;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.notEqualTo;

public class GcpWeakSSLProtocolCheckPart extends AbstractNewResourceCheck {

  @Override
  protected void registerResourceConsumer() {
    register("google_compute_ssl_policy",
      resource -> {
        AttributeSymbol minTlsVersion = resource.attribute("min_tls_version");
        minTlsVersion.reportIf(equalTo("TLS_1_0").or(equalTo("TLS_1_1")), WEAK_SSL_MESSAGE);
        if (minTlsVersion.isAbsent()) {
          AttributeSymbol profile = resource.attribute("profile");
          if (profile.isAbsent() || profile.is(notEqualTo("RESTRICTED"))) {
            minTlsVersion.reportIfAbsent(OMITTING_WEAK_SSL_MESSAGE);
          }
        }
      });
  }
}
