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
package org.sonar.iac.helm.utils;

import java.util.Locale;
import java.util.Set;

public class NativeUtils {
  /**
   * Platforms, for which sonar-helm-for-iac is built.
   */
  public static final Set<String> SUPPORTED_PLATFORMS = Set.of("darwin-amd64", "darwin-arm64", "windows-amd64", "linux-amd64");

  public String getSuffixForCurrentPlatform() {
    String platform = getNormalizedOsName(System.getProperty("os.name")) + "-" + getNormalizedArchName(System.getProperty("os.arch"));
    if (!SUPPORTED_PLATFORMS.contains(platform)) {
      throw new IllegalStateException("Unsupported platform: " + platform);
    }
    return platform;
  }

  /**
   * Normalize OS name, e.g. map `windows server 2020` to `windows`
   */
  public String getNormalizedOsName(String os) {
    os = os.toLowerCase(Locale.ROOT);
    if (os.startsWith("mac") || os.startsWith("darwin")) {
      os = "darwin";
    } else if (os.startsWith("win")) {
      os = "windows";
    } else if (os.startsWith("linux")) {
      os = "linux";
    } else {
      throw new IllegalStateException("Unsupported OS: " + os);
    }
    return os;
  }

  public String getNormalizedArchName(String arch) {
    arch = arch.toLowerCase(Locale.ROOT);
    if ("x86_64".equals(arch) || "amd64".equals(arch)) {
      arch = "amd64";
    } else if ("aarch64".equals(arch) || "arm64".equals(arch)) {
      arch = "arm64";
    } else {
      throw new IllegalStateException("Unsupported architecture: " + arch);
    }
    return arch;
  }
}
