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
package org.sonar.iac.springconfig.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.springconfig.parser.SpringConfigParser;

import static org.sonar.iac.common.testing.TemplateFileReader.BASE_DIR;

public final class SpringConfigVerifier {
  private SpringConfigVerifier() {
  }

  private static final SensorContextTester sensorContext = SensorContextTester.create(BASE_DIR.toAbsolutePath());

  private static final TreeParser<Tree> PARSER = new SpringConfigParser();

  public static void verify(String filename, IacCheck check) {
    var path = BASE_DIR.resolve(filename);
    Tree root = parseSpringConfig(path);
    Verifier.verify(root, path, check);
  }

  public static void verifyNoIssue(String filename, IacCheck check) {
    var path = BASE_DIR.resolve(filename);
    Tree root = parseSpringConfig(path);
    Verifier.verifyNoIssue(root, path, check);
  }

  private static InputFileContext createContextForFile(String filename) {
    var file = IacTestUtils.inputFile(filename, BASE_DIR);
    sensorContext.fileSystem().add(file);
    return new InputFileContext(sensorContext, file);
  }

  private static Tree parseSpringConfig(Path path) {
    var filename = path.getFileName().toString();
    var ctx = createContextForFile(filename);
    var content = readContent(path);

    return Verifier.parse(PARSER, content, ctx);
  }

  private static String readContent(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
