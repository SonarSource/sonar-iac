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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.testing.Verifier;
import org.sonarsource.analyzer.commons.checks.verifier.SingleFileVerifier;

public class ArmVerifier {

  private static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");

  private static final TreeParser<Tree> PARSER = new ArmParser();

  private ArmVerifier() {

  }

  public static void verify(String fileName, IacCheck check) {
    Verifier.verify(PARSER, BASE_DIR.resolve(fileName), check);
    Function<SingleFileVerifier, Verifier.TestContext> ctxSupplier = null;
    Verifier.verify(PARSER, BASE_DIR.resolve(fileName), check, ctxSupplier);
  }

  public static void verify(String fileName, IacCheck check, Verifier.Issue... expectedIssues) {
    Verifier.verify(PARSER, BASE_DIR.resolve(fileName), check, expectedIssues);
  }

  public static void verifyNoIssue(String fileName, IacCheck check) {
    Verifier.verifyNoIssue(PARSER, BASE_DIR.resolve(fileName), check);
  }

  public static List<Verifier.Issue> issues(String fileName, IacCheck check) {
    return Verifier.issues(PARSER, BASE_DIR.resolve(fileName), check);
  }

  private static String readFile(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read " + path, e);
    }
  }

}
