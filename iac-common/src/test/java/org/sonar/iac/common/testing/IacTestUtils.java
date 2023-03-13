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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.internal.apachecommons.lang.StringUtils;

public class IacTestUtils {

  private IacTestUtils() {
    // utils class
  }

  public static String code(String... lines) {
    return StringUtils.join(lines, System.getProperty("line.separator"));
  }

  public static DefaultInputFile inputFile(String fileName, String language) {
    try {
      return TestInputFileBuilder.create("moduleKey", fileName)
        .setModuleBaseDir(new File("src/test/resources").toPath())
        .setCharset(Charset.defaultCharset())
        .setLanguage(language)
        .initMetadata(java.nio.file.Files.readString(new File("src/test/resources/" + fileName).toPath())).build();
    } catch (IOException e) {
      throw new IllegalStateException("File not found", e);
    }
  }
}
