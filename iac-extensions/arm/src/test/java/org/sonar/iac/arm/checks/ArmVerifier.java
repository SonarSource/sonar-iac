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
package org.sonar.iac.arm.checks;

import java.nio.file.Path;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.visitors.ArmSymbolVisitor;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.common.testing.Verifier;

import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.testing.TemplateFileReader.BASE_DIR;

public class ArmVerifier {

  private static final TreeParser<Tree> PARSER = new ArmParser();

  private ArmVerifier() {

  }

  public static void verify(String fileName, IacCheck check, Verifier.Issue... expectedIssues) {
    verify(BASE_DIR.resolve(fileName), check, expectedIssues);
  }

  public static void verifyContent(String content, IacCheck check, Verifier.Issue... expectedIssues) {
    Path path = Verifier.contentToTmp(content).toPath();
    verify(path, check, expectedIssues);
  }

  private static void verify(Path path, IacCheck check, Verifier.Issue... expectedIssues) {
    Tree root = Verifier.parse(PARSER, path);
    ArmSymbolVisitor symbolVisitor = new ArmSymbolVisitor();
    var inputFileContext = IacTestUtils.createInputFileContextMockFromContent(Verifier.readFile(path), path.toFile().getName(), "");
    symbolVisitor.scan(inputFileContext, root);
    Verifier.verify(root, path, check, expectedIssues);
  }

  public static void verifyNoIssue(String fileName, IacCheck check) {
    Path path = BASE_DIR.resolve(fileName);
    Tree root = Verifier.parse(PARSER, path);
    ArmSymbolVisitor symbolVisitor = new ArmSymbolVisitor();
    symbolVisitor.scan(mock(InputFileContext.class), root);
    Verifier.verifyNoIssue(root, path, check);
  }
}
