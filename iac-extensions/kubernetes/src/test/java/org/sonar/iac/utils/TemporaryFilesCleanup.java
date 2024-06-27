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
package org.sonar.iac.utils;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.sonar.iac.kubernetes.checks.KubernetesVerifier;

public class TemporaryFilesCleanup implements AfterAllCallback {
  @Override
  public void afterAll(ExtensionContext extensionContext) throws Exception {
    try (var stream = Files.walk(KubernetesVerifier.BASE_DIR)) {
      stream.filter(Files::isRegularFile)
        .filter(file -> file.getFileName().toString().equals(KubernetesVerifier.TMP_CONTENT_FILE_NAME))
        .forEach(file -> {
          try {
            Files.delete(file);
          } catch (IOException e) {
            throw new IllegalStateException("Failed to delete temporary file: " + file, e);
          }
        });
    }
  }
}
