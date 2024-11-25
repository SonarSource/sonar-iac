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
package org.sonar.iac.docker.checks;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.testing.TemplateFileReader;

class PackageInstallationCacheCheckTest {

  @Test
  void shouldCheckWithAllCommandsMixed() {
    DockerVerifier.verify("PackageInstallationCacheCheck/mixed.dockerfile", new PackageInstallationCacheCheck());
  }

  @Test
  void shouldCheckOnMultiStages() {
    DockerVerifier.verify("PackageInstallationCacheCheck/multi_stage.dockerfile", new PackageInstallationCacheCheck());
  }

  @Test
  void shouldCheckOnMultiStagesWithDependencies() {
    DockerVerifier.verify("PackageInstallationCacheCheck/multi_stage_with_dependencies.dockerfile", new PackageInstallationCacheCheck());
  }

  @Test
  void shouldCheckOnMultiStagesWithCircularDependencies() {
    DockerVerifier.verify("PackageInstallationCacheCheck/multi_stage_with_circular_dependencies.dockerfile", new PackageInstallationCacheCheck());
  }

  @MethodSource
  @ParameterizedTest(name = "test for command: {0}")
  void shouldCheckOnCommandsTemplate(String commandName, String installCommand, String rmLocation, String cleanCacheCommand) {
    String[] replacements = new String[] {
      "{$commandName}", commandName,
      "{$installCommand}", installCommand,
      "{$rmLocation}", rmLocation,
      "{$cacheCleanCommand}", cleanCacheCommand};

    String content = TemplateFileReader.readTemplateAndReplace("PackageInstallationCacheCheck/packageInstall_template.dockerfile", replacements);
    DockerVerifier.verifyContent(content, new PackageInstallationCacheCheck());
  }

  private static Stream<Arguments> shouldCheckOnCommandsTemplate() {
    return Stream.of(
      Arguments.of("apk", "add", "/etc/apk/cache/*", "cache clean"),
      Arguments.of("apk", "add", "/var/cache/apk/*", "cache clean"),
      Arguments.of("apt", "install", "/var/lib/apt/lists/*", "clean"),
      Arguments.of("apt-get", "install", "/var/lib/apt/lists/*", "clean"),
      Arguments.of("aptitude", "install", "/var/lib/apt/lists/*", "clean"));
  }
}
