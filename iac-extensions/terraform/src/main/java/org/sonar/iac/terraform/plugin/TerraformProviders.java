/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.terraform.plugin;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Optional;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.api.sonarlint.SonarLintSide;

import static org.sonar.iac.common.warnings.DefaultAnalysisWarningsWrapper.NOOP_ANALYSIS_WARNINGS;

@ScannerSide
@SonarLintSide
public class TerraformProviders {

  private static final String MISSING_PROVIDER_VERSION = "Provide the used %s provider version via the \"%s\" property to increase the accuracy of your results.";
  private static final String INVALID_PROVIDER_VERSION = "Can not parse provider version for \"%s\". " +
    "Please check the format of your used %s version in the project settings.";

  private static final Logger LOG = LoggerFactory.getLogger(TerraformProviders.class);

  private static final Provider UNKNOWN_PROVIDER = new Provider(null);

  private final EnumMap<Provider.Identifier, Provider> providers = new EnumMap<>(Provider.Identifier.class);

  private final AnalysisWarningsWrapper analysisWarnings;

  private final EnumSet<Provider.Identifier> raisedWarnings = EnumSet.noneOf(Provider.Identifier.class);

  public TerraformProviders(Configuration config) {
    this(config, NOOP_ANALYSIS_WARNINGS);
  }

  public TerraformProviders(Configuration config, AnalysisWarningsWrapper analysisWarnings) {
    this.analysisWarnings = analysisWarnings;
    for (Provider.Identifier identifier : Provider.Identifier.values()) {
      config.get(identifier.key)
        .flatMap(version -> parseProviderVersion(identifier, version))
        .map(Provider::new)
        .ifPresent(provider -> providers.put(identifier, provider));
    }
  }

  private Optional<Version> parseProviderVersion(Provider.Identifier identifier, String version) {
    if (version.trim().isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.of(Version.parse(version));
    } catch (IllegalArgumentException e) {
      raiseWarning(identifier, String.format(INVALID_PROVIDER_VERSION, identifier.key, identifier.name));
      LOG.warn("Can not parse provider version \"{}\". Input: \"{}\"", identifier.key, version);
      return Optional.empty();
    }
  }

  private void raiseWarning(Provider.Identifier identifier, String text) {
    if (raisedWarnings.add(identifier)) {
      analysisWarnings.addWarning(text);
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
      AWS("sonar.terraform.provider.aws.version", "AWS"),
      AZURE("sonar.terraform.provider.azure.version", "Azure");

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
      if (isUnknown()) {
        return false;
      }
      return providerVersion.compareTo(version) < 0;
    }

    public boolean isUnknown() {
      return providerVersion == null;
    }
  }
}
