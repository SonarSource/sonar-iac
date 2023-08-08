/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.terraform.plugin;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.terraform.plugin.TerraformProviders.Provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TerraformProvidersTest {

  private final AnalysisWarningsWrapper analysisWarnings = mock(AnalysisWarningsWrapper.class);

  private static final String AWS_KEY = "sonar.terraform.provider.aws.version";
  @TempDir
  protected File baseDir;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @Test
  void provider_with_valid_version() {
    TerraformProviders providers = providers(context(AWS_KEY, "1.3.4"));
    Provider provider = providers.provider(Provider.Identifier.AWS);
    assertThat(provider.providerVersion).isEqualTo(Version.parse("1.3.4"));
    verifyNoInteractions(analysisWarnings);
  }

  @Test
  void provider_with_invalid_version() {
    TerraformProviders providers = providers(context(AWS_KEY, "v1.3.4"));
    Provider provider = providers.provider(Provider.Identifier.AWS);

    assertThat(provider.providerVersion).isNull();
    assertThat(logTester.logs(Level.WARN))
      .containsExactly("Can not parse provider version \"sonar.terraform.provider.aws.version\". Input: \"v1.3.4\"");
    verify(analysisWarnings, times(1))
      .addWarning("Can not parse provider version for \"sonar.terraform.provider.aws.version\". " +
        "Please check the format of your used AWS version in the project settings.");
  }

  @Test
  void provider_with_empty_version() {
    TerraformProviders providers = providers(context(AWS_KEY, ""));
    Provider provider = providers.provider(Provider.Identifier.AWS);
    assertThat(provider.providerVersion).isNull();
    verify(analysisWarnings, times(1))
      .addWarning("Provide the used AWS provider version via the \"sonar.terraform.provider.aws.version\" " +
        "property to increase the accuracy of your results.");
  }

  @Test
  void provider_without_provided_version() {
    TerraformProviders providers = providers(SensorContextTester.create(baseDir));
    providers.provider(Provider.Identifier.AWS);
    verify(analysisWarnings, times(1))
      .addWarning("Provide the used AWS provider version via the \"sonar.terraform.provider.aws.version\" " +
        "property to increase the accuracy of your results.");
  }

  @Test
  void single_warning_for_missing_provided_version() {
    TerraformProviders providers = providers(SensorContextTester.create(baseDir));
    providers.provider(Provider.Identifier.AWS);
    providers.provider(Provider.Identifier.AWS);
    verify(analysisWarnings, times(1))
      .addWarning("Provide the used AWS provider version via the \"sonar.terraform.provider.aws.version\" " +
        "property to increase the accuracy of your results.");
  }

  @Test
  void hasVersionLowerThan() {
    Provider provider = new Provider(Version.create(1, 3, 4));
    assertThat(provider.hasVersionLowerThan(Version.create(1, 3, 4))).isFalse();
    assertThat(provider.hasVersionLowerThan(Version.create(1, 0))).isFalse();
    assertThat(provider.hasVersionLowerThan(Version.create(1, 2, 5))).isFalse();
    assertThat(provider.hasVersionLowerThan(Version.create(1, 4, 3))).isTrue();
    assertThat(provider.hasVersionLowerThan(Version.create(2, 0))).isTrue();
    verifyNoInteractions(analysisWarnings);
  }

  @Test
  void assess_without_provider_version() {
    Provider provider = new Provider(null);
    assertThat(provider.hasVersionLowerThan(Version.create(2, 0))).isFalse();
    verifyNoInteractions(analysisWarnings);
  }

  @Test
  void check_property_registration() {
    long registeredProperties = TerraformSettings.getGeneralProperties().stream()
      .filter(propertyDefinition -> propertyDefinition.key().startsWith("sonar.terraform.provider"))
      .count();

    assertThat(registeredProperties).isEqualTo(Provider.Identifier.values().length);
  }

  @Test
  void checkGeneralPropertiesPrefix() {
    long countPrefix = TerraformSettings.getGeneralProperties().stream()
      .filter(propertyDefinition -> propertyDefinition.key().startsWith("sonar.terraform."))
      .count();

    assertThat(countPrefix).isEqualTo(TerraformSettings.getGeneralProperties().size());
  }

  @Test
  void checkExternalReportPropertiesPrefix() {
    long countPrefix = TerraformSettings.getExternalReportProperties().stream()
      .filter(propertyDefinition -> propertyDefinition.key().startsWith("sonar.terraform."))
      .count();

    assertThat(countPrefix).isEqualTo(TerraformSettings.getExternalReportProperties().size());
  }

  private TerraformProviders providers(SensorContext context) {
    return new TerraformProviders(context, analysisWarnings);
  }

  private SensorContext context(String key, String value) {
    MapSettings settings = new MapSettings();
    settings.setProperty(key, value);
    SensorContextTester context = SensorContextTester.create(baseDir);
    context.setSettings(settings);
    return context;
  }
}
