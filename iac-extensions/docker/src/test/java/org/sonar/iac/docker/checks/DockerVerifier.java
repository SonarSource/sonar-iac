/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import java.nio.file.Path;
import java.nio.file.Paths;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.docker.parser.DockerParser;

public class DockerVerifier {

  private DockerVerifier() {

  }

  private static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");
  private static final DockerParser PARSER = new DockerParser();

  public static void verify(String fileName, IacCheck check) {
    Verifier.verify(PARSER, BASE_DIR.resolve(fileName), check, Verifier.TestContext::new);
  }
}
