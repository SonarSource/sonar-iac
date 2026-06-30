/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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

  private static final String CTX_DATABASES = "azure_databases";
  private static final String CTX_STORAGE_ACCOUNTS = "azure_storage_accounts";

  @Override
  protected void registerResourceConsumer() {
    register(List.of("azurerm_postgresql_server", "azurerm_mysql_server"),
      checkSSLProtocol("ssl_minimal_tls_version_enforced", CTX_DATABASES));

    register("azurerm_storage_account",
      checkSSLProtocol("min_tls_version", CTX_STORAGE_ACCOUNTS));

    register(List.of("azurerm_mssql_server"),
      checkSSLProtocolIgnoreAbsent("minimum_tls_version", CTX_DATABASES));
  }

  private static Consumer<ResourceSymbol> checkSSLProtocol(String protocolAttribute, String contextKey) {
    return (ResourceSymbol resource) -> {
      var protocolAttributeSymbol = resource.attribute(protocolAttribute);
      protocolAttributeSymbol.reportIf(notEqualTo("TLS1_2"), WEAK_SSL_MESSAGE, contextKey);

      boolean shouldReportIfAbsent = resource.provider(AZURE).hasVersionLowerThan(AZURE_VERSION_WITH_SECURE_SETTING_BY_DEFAULT);
      if (shouldReportIfAbsent) {
        protocolAttributeSymbol.reportIfAbsent(OMITTING_WEAK_SSL_MESSAGE, contextKey);
      }
    };
  }

  private static Consumer<ResourceSymbol> checkSSLProtocolIgnoreAbsent(String protocolAttribute, String contextKey) {
    return resource -> resource.attribute(protocolAttribute)
      .reportIf(notEqualTo("1.2"), WEAK_SSL_MESSAGE, contextKey);
  }
}
