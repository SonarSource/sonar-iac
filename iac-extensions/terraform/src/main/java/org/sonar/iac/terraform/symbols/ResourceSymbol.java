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
package org.sonar.iac.terraform.symbols;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.plugin.TerraformProviders.Provider;
import org.sonar.iac.terraform.visitors.TerraformProviderContext;

import static org.sonar.iac.terraform.checks.AbstractNewResourceCheck.isResource;

public class ResourceSymbol extends BlockSymbol {

  // Supported argument pairs from the HashiCorp AWS, AzureRM, and Google provider schemas.
  private static final Map<String, Set<String>> KNOWN_WRITE_ONLY_COUNTERPARTS = Map.ofEntries(
    // hashicorp/aws
    Map.entry("aws_db_instance", Set.of("password")),
    Map.entry("aws_docdb_cluster", Set.of("master_password")),
    Map.entry("aws_rds_cluster", Set.of("master_password")),
    Map.entry("aws_redshift_cluster", Set.of("master_password")),
    Map.entry("aws_secretsmanager_secret_version", Set.of("secret_string")),
    Map.entry("aws_ssm_parameter", Set.of("value")),
    // hashicorp/azurerm
    Map.entry("azurerm_key_vault_secret", Set.of("value")),
    // hashicorp/google
    Map.entry("google_secret_manager_secret_version", Set.of("secret_data")),
    Map.entry("google_sql_user", Set.of("password")));

  public final String type;

  private ResourceSymbol(CheckContext ctx, BlockTree tree) {
    super(ctx, tree, tree.labels().size() < 2 ? "unknown" : tree.labels().get(1).value(), null);
    type = tree.labels().isEmpty() ? "" : tree.labels().get(0).value();
  }

  public static ResourceSymbol fromPresent(CheckContext ctx, BlockTree tree) {
    return new ResourceSymbol(ctx, tree);
  }

  public Provider provider(Provider.Identifier identifier) {
    return ((TerraformProviderContext) ctx).provider(identifier);
  }

  boolean isResourceDeclaration() {
    return tree != null && isResource(tree);
  }

  /** Returns a provider-supported or locally observed write-only counterpart for this resource type. */
  public Optional<String> writeOnlyCounterpart(String persistedArgument) {
    if (!isResourceDeclaration()) {
      return Optional.empty();
    }
    String counterpart = persistedArgument + AttributeSymbol.WRITE_ONLY_SUFFIX;
    boolean supported = KNOWN_WRITE_ONLY_COUNTERPARTS.getOrDefault(type, Set.of()).contains(persistedArgument)
      || attribute(counterpart).isWriteOnly();
    return supported ? Optional.of(counterpart) : Optional.empty();
  }

  @Override
  public ResourceSymbol reportIfAbsent(String message, SecondaryLocation... secondaries) {
    throw new UnsupportedOperationException("Resource symbols should always exists");
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree.labels().isEmpty() ? null : tree.labels().get(0);
  }
}
