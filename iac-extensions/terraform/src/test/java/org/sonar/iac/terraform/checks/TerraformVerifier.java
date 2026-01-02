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
package org.sonar.iac.terraform.checks;

import java.util.function.Function;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.iac.terraform.plugin.TerraformProviders.Provider;
import org.sonar.iac.terraform.visitors.TerraformProviderContext;
import org.sonarsource.analyzer.commons.checks.verifier.MultiFileVerifier;

import static org.sonar.iac.common.testing.TemplateFileReader.BASE_DIR;

public class TerraformVerifier {

  private TerraformVerifier() {

  }

  private static final HclParser PARSER = new HclParser();

  public static void verify(String fileName, IacCheck check) {
    Verifier.verify(PARSER, BASE_DIR.resolve(fileName), check, TerraformTestContext::new);
  }

  public static void verifyWithProviderVersion(String fileName, IacCheck check, String providerVersion) {
    Verifier.verify(PARSER, BASE_DIR.resolve(fileName), check, context(providerVersion));
  }

  public static void verifyNoIssue(String fileName, IacCheck check) {
    Verifier.verifyNoIssue(PARSER, BASE_DIR.resolve(fileName), check, TerraformTestContext::new);
  }

  public static void verifyNoIssueWithProviderVersion(String fileName, IacCheck check, String providerVersion) {
    Verifier.verifyNoIssue(PARSER, BASE_DIR.resolve(fileName), check, context(providerVersion));
  }

  private static Function<MultiFileVerifier, Verifier.TestContext> context(String providerVersion) {
    return verifier -> new TerraformTestContext(verifier, providerVersion);
  }

  public static class TerraformTestContext extends Verifier.TestContext implements TerraformProviderContext {

    final Provider provider;

    public TerraformTestContext(MultiFileVerifier verifier) {
      super(verifier);
      this.provider = new Provider(null);
    }

    public TerraformTestContext(MultiFileVerifier verifier, String providerVersion) {
      super(verifier);
      this.provider = new Provider(Version.parse(providerVersion));
    }

    @Override
    public Provider provider(Provider.Identifier p) {
      return this.provider;
    }
  }
}
