/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.docker.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.TemplateFileReader;

class SecretsGenerationCheckTest {

  final IacCheck check = new SecretsGenerationCheck();

  @Test
  void shouldCheckSshKeygen() {
    DockerVerifier.verify("SecretsGenerationCheck/sshKeygen.dockerfile", new SecretsGenerationCheck());
  }

  @Test
  void shouldCheckKeytool() {
    DockerVerifier.verify("SecretsGenerationCheck/keytool.dockerfile", check);
  }

  @Test
  void shouldCheckOpenssl() {
    DockerVerifier.verify("SecretsGenerationCheck/openssl.dockerfile", check);
  }

  @ValueSource(strings = {"--password", "--ftp-password", "--http-password", "--proxy-password"})
  @ParameterizedTest
  void shouldCheckWget(String flag) {
    String content = TemplateFileReader.readTemplateAndReplace("SecretsGenerationCheck/wget_template.dockerfile", "--flag", flag);
    DockerVerifier.verifyContent(content, check);
  }

  @Test
  void shouldCheckWgetLocation() {
    DockerVerifier.verify("SecretsGenerationCheck/wget_location.dockerfile", check);
  }

  @Test
  void shouldCheckCurl() {
    DockerVerifier.verify("SecretsGenerationCheck/curl.dockerfile", check);
  }

  @Test
  void shouldCheckSshpass() {
    DockerVerifier.verify("SecretsGenerationCheck/sshpass.dockerfile", check);
  }

  @Test
  void shouldCheckHtpasswd() {
    DockerVerifier.verify("SecretsGenerationCheck/htpasswd.dockerfile", check);
  }

  @ValueSource(strings = {"mysql", "mysqladmin", "mysqldump"})
  @ParameterizedTest
  void shouldCheckMySqlCommands(String command) {
    String content = TemplateFileReader.readTemplateAndReplace("SecretsGenerationCheck/mysql_template.dockerfile", "mysql", command);
    DockerVerifier.verifyContent(content, check);
  }

  @Test
  void shouldCheckMySqlLocations() {
    DockerVerifier.verify("SecretsGenerationCheck/mysql_locations.dockerfile", check);
  }

  @Test
  void shouldCheckUseradd() {
    DockerVerifier.verify("SecretsGenerationCheck/useradd.dockerfile", check);
  }

  @Test
  void shouldCheckUsermod() {
    DockerVerifier.verify("SecretsGenerationCheck/usermod.dockerfile", check);
  }

  @Test
  void shouldCheckNetUser() {
    DockerVerifier.verify("SecretsGenerationCheck/net_user.dockerfile", check);
  }

  @Test
  void shouldCheckDrush() {
    DockerVerifier.verify("SecretsGenerationCheck/drush.dockerfile", check);
  }

  @Test
  void shouldCheckX11Vnc() {
    DockerVerifier.verify("SecretsGenerationCheck/x11vnc.dockerfile", check);
  }
}
