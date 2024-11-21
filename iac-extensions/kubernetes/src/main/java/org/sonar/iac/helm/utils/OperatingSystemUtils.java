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
package org.sonar.iac.helm.utils;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class OperatingSystemUtils {
  /**
   * Platforms, for which sonar-helm-for-iac is built.
   */
  public static final Set<String> SUPPORTED_PLATFORMS = Set.of("darwin-amd64", "darwin-arm64", "windows-amd64", "linux-amd64");

  private OperatingSystemUtils() {
  }

  public static Optional<String> getCurrentPlatformIfSupported() {
    String platform = getNormalizedOsName(System.getProperty("os.name")) + "-" + getNormalizedArchName(System.getProperty("os.arch"));
    return Optional.of(platform).filter(SUPPORTED_PLATFORMS::contains);
  }

  /**
   * Normalize OperatingSystem name, e.g. map `windows server 2020` to `windows`
   */
  static String getNormalizedOsName(String operatingSystem) {
    operatingSystem = operatingSystem.toLowerCase(Locale.ROOT);
    if (operatingSystem.startsWith("mac") || operatingSystem.startsWith("darwin")) {
      operatingSystem = "darwin";
    } else if (operatingSystem.startsWith("win")) {
      operatingSystem = "windows";
    } else if (operatingSystem.startsWith("linux")) {
      operatingSystem = "linux";
    } else {
      throw new IllegalStateException("Unsupported OS: " + operatingSystem);
    }
    return operatingSystem;
  }

  static String getNormalizedArchName(String architecture) {
    architecture = architecture.toLowerCase(Locale.ROOT);
    if ("x86_64".equals(architecture) || "amd64".equals(architecture)) {
      architecture = "amd64";
    } else if ("aarch64".equals(architecture) || "arm64".equals(architecture)) {
      architecture = "arm64";
    } else {
      throw new IllegalStateException("Unsupported architecture: " + architecture);
    }
    return architecture;
  }
}
