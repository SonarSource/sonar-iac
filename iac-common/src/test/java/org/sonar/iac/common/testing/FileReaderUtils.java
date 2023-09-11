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
package org.sonar.iac.common.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReaderUtils {

  public static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");

  public static String readTemplateAndReplace(String path, String type) {
    String content;
    try {
      content = Files.readString(BASE_DIR.resolve(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return content.replace("${type}", type);
  }

  public static String readTemplateAndReplace(String path, String... types) {
    if (types.length % 2 == 1) {
      throw new RuntimeException("There should be even number of strings");
    }
    String content;
    try {
      content = Files.readString(BASE_DIR.resolve(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    for (int i = 0; i < types.length; i = i + 2) {
      content = content.replace(types[i], types[i + 1]);
    }
    return content;
  }

}
