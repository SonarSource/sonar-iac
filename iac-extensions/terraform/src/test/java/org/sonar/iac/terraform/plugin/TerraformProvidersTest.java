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
package org.sonar.iac.terraform.plugin;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.terraform.plugin.TerraformProviders.Provider;

import static org.assertj.core.api.Assertions.assertThat;

class TerraformProvidersTest {


  private static final String AWS_KEY = "sonar.terraform.provider.version.aws";
  @TempDir
  protected File baseDir;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void parse_valid_version() {
    TerraformProviders providerVersions = new TerraformProviders(context(AWS_KEY, "1.3.4"));
    Provider provider =  providerVersions.provider(Provider.Identifier.AWS);
    assertThat(provider.providerVersion).isEqualTo(Version.parse("1.3.4"));
  }

  @Test
  void parse_invalid_version() {
    TerraformProviders providerVersions = new TerraformProviders(context(AWS_KEY, "v1.3.4"));
    Provider provider =  providerVersions.provider(Provider.Identifier.AWS);
    assertThat(provider.providerVersion).isNull();
    assertThat(logTester.logs(LoggerLevel.WARN))
      .containsExactly("Can not parse provider version \"sonar.terraform.provider.version.aws\".");
  }

  @Test
  void parse_empty_version() {
    TerraformProviders providerVersions = new TerraformProviders(context(AWS_KEY, ""));
    Provider provider =  providerVersions.provider(Provider.Identifier.AWS);
    assertThat(provider.providerVersion).isNull();
  }

  @Test
  void isLower() {
    Provider provider = new Provider(Version.parse("1.3.4"));
    assertThat(provider.isLower("1.3.4")).isFalse();
    assertThat(provider.isLower("1")).isFalse();
    assertThat(provider.isLower("1.2.5")).isFalse();
    assertThat(provider.isLower("1.4.3")).isTrue();
    assertThat(provider.isLower("2")).isTrue();
  }

  @Test
  void assess_without_provider_version() {
    Provider provider = new Provider(null);
    assertThat(provider.isLower("2")).isFalse();
  }

  @Test
  void assess_with_invalid_version() {
    Provider provider = new Provider(Version.parse("1.3.4"));
    assertThat(provider.isLower("v2")).isFalse();

    assertThat(logTester.logs(LoggerLevel.DEBUG))
      .containsExactly("Can not parse version \"v2\" for provider verification.");
  }

  private SensorContext context(String key, String value) {
    MapSettings settings = new MapSettings();
    settings.setProperty(key, value);
    SensorContextTester context = SensorContextTester.create(baseDir);
    context.setSettings(settings);
    return context;
  }
}
