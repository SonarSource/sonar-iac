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
