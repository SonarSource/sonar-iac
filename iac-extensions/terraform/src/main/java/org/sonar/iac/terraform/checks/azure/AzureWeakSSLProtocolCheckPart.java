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
package org.sonar.iac.terraform.checks.azure;

import java.util.List;
import java.util.function.Consumer;
import org.sonar.api.utils.Version;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.checks.WeakSSLProtocolCheck.OMITTING_WEAK_SSL_MESSAGE;
import static org.sonar.iac.terraform.checks.WeakSSLProtocolCheck.WEAK_SSL_MESSAGE;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.notEqualTo;
import static org.sonar.iac.terraform.plugin.TerraformProviders.Provider.Identifier.AZURE;

public class AzureWeakSSLProtocolCheckPart extends AbstractNewResourceCheck {
  private static final Version AZURE_VERSION_WITH_SECURE_SETTING_BY_DEFAULT = Version.create(3, 0);

  @Override
  protected void registerResourceConsumer() {
    register(List.of("azurerm_postgresql_server", "azurerm_mysql_server"),
      checkSSLProtocol("ssl_minimal_tls_version_enforced"));

    register("azurerm_storage_account",
      checkSSLProtocol("min_tls_version"));

    register(List.of("azurerm_mssql_server"),
      checkSSLProtocolIgnoreAbsent("minimum_tls_version"));
  }

  private static Consumer<ResourceSymbol> checkSSLProtocol(String protocolAttribute) {
    return (ResourceSymbol resource) -> {
      var protocolAttributeSymbol = resource.attribute(protocolAttribute);
      protocolAttributeSymbol.reportIf(notEqualTo("TLS1_2"), WEAK_SSL_MESSAGE);

      boolean shouldReportIfAbsent = resource.provider(AZURE).hasVersionLowerThan(AZURE_VERSION_WITH_SECURE_SETTING_BY_DEFAULT);
      if (shouldReportIfAbsent) {
        protocolAttributeSymbol.reportIfAbsent(OMITTING_WEAK_SSL_MESSAGE);
      }
    };
  }

  private static Consumer<ResourceSymbol> checkSSLProtocolIgnoreAbsent(String protocolAttribute) {
    return resource -> resource.attribute(protocolAttribute)
      .reportIf(notEqualTo("1.2"), WEAK_SSL_MESSAGE);
  }
}
