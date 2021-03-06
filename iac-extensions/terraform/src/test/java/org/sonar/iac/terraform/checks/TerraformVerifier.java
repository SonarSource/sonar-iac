/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.terraform.checks;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.iac.terraform.plugin.TerraformProviders.Provider;
import org.sonar.iac.terraform.visitors.TerraformProviderContext;
import org.sonarsource.analyzer.commons.checks.verifier.SingleFileVerifier;

public class TerraformVerifier {

  private TerraformVerifier() {

  }

  private static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");
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

  private static Function<SingleFileVerifier, Verifier.TestContext> context(String providerVersion) {
    return verifier -> new TerraformTestContext(verifier, providerVersion);
  }

  public static class TerraformTestContext extends Verifier.TestContext implements TerraformProviderContext {

    final Provider provider;

    public TerraformTestContext(SingleFileVerifier verifier) {
      super(verifier);
      this.provider = new Provider(null);
    }

    public TerraformTestContext(SingleFileVerifier verifier, String providerVersion) {
      super(verifier);
      this.provider = new Provider(Version.parse(providerVersion));
    }

    @Override
    public Provider provider(Provider.Identifier p) {
      return this.provider;
    }
  }
}
