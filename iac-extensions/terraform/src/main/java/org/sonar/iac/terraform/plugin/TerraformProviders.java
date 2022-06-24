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

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.notifications.AnalysisWarnings;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@ScannerSide
public class TerraformProviders {

  private static final String MISSING_PROVIDER_VERSION = "Provide the used %s provider version via the \"%s\" property to increase the accuracy of your results.";
  private static final String INVALID_PROVIDER_VERSION = "Can not parse provider version for \"%s\". Please check format.";

  private static final Logger LOG = Loggers.get(TerraformProviders.class);

  private static final Provider UNKNOWN_PROVIDER = new Provider(null);

  private final EnumMap<Provider.Identifier, Provider> providers = new EnumMap<>(Provider.Identifier.class);

  private final AnalysisWarnings analysisWarnings;

  private final Set<Provider.Identifier> raisedWarnings = new HashSet<>();

  public TerraformProviders(SensorContext sensorContext, AnalysisWarnings analysisWarnings) {
    this.analysisWarnings = analysisWarnings;
    for (Provider.Identifier identifier : Provider.Identifier.values()) {
      sensorContext.config().get(identifier.key)
        .flatMap(version -> parseProviderVersion(identifier, version))
        .map(Provider::new)
        .ifPresent(provider -> providers.put(identifier, provider));
    }
  }
  private Optional<Version> parseProviderVersion(Provider.Identifier identifier, String version) {
    if (version.trim().isEmpty()) {
      return Optional.empty();
    }
    try{
      return Optional.of(Version.parse(version));
    } catch (IllegalArgumentException e) {
      raiseWarning(identifier, String.format(INVALID_PROVIDER_VERSION, identifier.key));
      LOG.warn("Can not parse provider version \"{}\".", identifier.key);
      return Optional.empty();
    }
  }

  private void raiseWarning(Provider.Identifier identifier, String text) {
    if (raisedWarnings.add(identifier)) {
      analysisWarnings.addUnique(text);
    }
  }

  public Provider provider(Provider.Identifier identifier) {
    if (providers.containsKey(identifier)) {
      return providers.get(identifier);
    } else {
      raiseWarning(identifier, String.format(MISSING_PROVIDER_VERSION, identifier.name, identifier.key));
      return UNKNOWN_PROVIDER;
    }
  }

  public static final class Provider {

    public enum Identifier {
      AWS("sonar.terraform.provider.aws.version", "AWS");

      public final String key;
      private final String name;

      Identifier(String key, String name) {
        this.key = key;
        this.name = name;
      }
    }

    final Version providerVersion;

    public Provider(@Nullable Version providerVersion) {
      this.providerVersion = providerVersion;
    }

    public boolean hasVersionLowerThan(Version version) {
      if (providerVersion ==  null) {
        return false;
      }
      return providerVersion.compareTo(version) < 0;
    }
  }
}
