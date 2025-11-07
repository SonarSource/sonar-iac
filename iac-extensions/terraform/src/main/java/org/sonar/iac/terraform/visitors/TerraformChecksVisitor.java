/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.terraform.visitors;

import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.terraform.plugin.TerraformProviders;
import org.sonar.iac.terraform.plugin.TerraformProviders.Provider;

public class TerraformChecksVisitor extends ChecksVisitor {
  private final TerraformProviders providerVersions;

  public TerraformChecksVisitor(Checks<IacCheck> checks, DurationStatistics statistics, TerraformProviders providerVersions) {
    super(checks, statistics);
    this.providerVersions = providerVersions;
  }

  @Override
  protected InitContext context(RuleKey ruleKey) {
    return new TerraformContextAdapter(ruleKey);
  }

  class TerraformContextAdapter extends ContextAdapter implements TerraformProviderContext {

    public TerraformContextAdapter(RuleKey ruleKey) {
      super(ruleKey);
    }

    @Override
    public Provider provider(Provider.Identifier identifier) {
      return providerVersions.provider(identifier);
    }
  }
}
