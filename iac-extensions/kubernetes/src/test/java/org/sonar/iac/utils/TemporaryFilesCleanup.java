/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
