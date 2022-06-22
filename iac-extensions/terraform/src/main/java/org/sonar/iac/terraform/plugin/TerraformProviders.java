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
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@ScannerSide
public class TerraformProviders {

  private static final Logger LOG = Loggers.get(TerraformProviders.class);

  private static final Provider UNKNOWN_PROVIDER = new Provider(null);

  private final EnumMap<Provider.Identifier, Provider> providers = new EnumMap<>(Provider.Identifier.class);

  public TerraformProviders(SensorContext sensorContext) {
    for (Provider.Identifier identifier : Provider.Identifier.values()) {
      sensorContext.config().get(identifier.key)
        .flatMap(version -> parseProviderVersion(identifier, version))
        .map(Provider::new)
        .ifPresent(provider -> providers.put(identifier, provider));
    }
  }
  private Optional<Version> parseProviderVersion(Provider.Identifier identifier, String version) {
    try{
      if (version.trim().isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(Version.parse(version));
    } catch (IllegalArgumentException e) {
      LOG.warn("Can not parse provider version \"{}\".", identifier.key);
      return Optional.empty();
    }
  }

  public Provider provider(Provider.Identifier identifier) {
    return providers.getOrDefault(identifier, UNKNOWN_PROVIDER);
  }

  public static final class Provider {

    public enum Identifier {
      AWS("sonar.terraform.provider.version.aws");

      public final String key;

      Identifier(String key) {
        this.key = key;
      }
    }

    final Version providerVersion;

    public Provider(@Nullable Version providerVersion) {
      this.providerVersion = providerVersion;
    }

    public boolean isLower(String version) {
      if (providerVersion ==  null) {
        return false;
      }
      return parseVersion(version).filter(v -> providerVersion.compareTo(v) < 0).isPresent();
    }
    
    private Optional<Version> parseVersion(String version) {
      try {
        return Optional.of(Version.parse(version));
      } catch (IllegalArgumentException e) {
        LOG.debug("Can not parse version \"{}\" for provider verification.", version);
        return Optional.empty();
      }
    }
  }
}
