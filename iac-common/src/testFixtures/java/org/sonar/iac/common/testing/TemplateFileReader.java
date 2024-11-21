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
package org.sonar.iac.common.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TemplateFileReader {

  public static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");

  public static String readContent(String path) {
    try {
      return Files.readString(BASE_DIR.resolve(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String readTemplateAndReplace(String path, String type) {
    String content = readContent(path);
    if (!content.contains("${type}")) {
      throw new RuntimeException("No variable \"${type}\" to replace found in the following template: " + path);
    }
    return content.replace("${type}", type);
  }

  public static String readTemplateAndReplace(String path, String... types) {
    if (types.length % 2 == 1) {
      throw new RuntimeException("There should be even number of strings");
    }
    String content = readContent(path);
    for (int i = 0; i < types.length; i = i + 2) {
      if (!content.contains(types[i])) {
        throw new RuntimeException("No variable \"" + types[i] + "\" to replace found in the following template: " + path);
      }
      content = content.replace(types[i], types[i + 1]);
    }
    return content;
  }

}
