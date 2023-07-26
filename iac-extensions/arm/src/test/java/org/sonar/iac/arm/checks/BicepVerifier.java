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
package org.sonar.iac.arm.checks;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.Verifier;

public class BicepVerifier {
  private static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");
  private static final BicepParser PARSER = BicepParser.create();

  public static void verify(String fileName, IacCheck check) {
    Verifier.verify(PARSER, BASE_DIR.resolve(fileName), check);
  }

  public static void verifyContent(String content, IacCheck check, Verifier.Issue... expectedIssues) {
    Verifier.verify(PARSER, content, check, expectedIssues);
  }

  public static void verifyNoIssue(String fileName, IacCheck check) {
    Verifier.verifyNoIssue(PARSER, BASE_DIR.resolve(fileName), check);
  }
}
