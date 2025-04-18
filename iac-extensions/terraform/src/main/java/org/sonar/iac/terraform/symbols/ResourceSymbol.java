/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.plugin.TerraformProviders.Provider;
import org.sonar.iac.terraform.visitors.TerraformProviderContext;

public class ResourceSymbol extends BlockSymbol {

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
