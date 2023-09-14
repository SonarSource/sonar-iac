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
package org.sonar.iac.docker.checks;

import java.nio.file.Path;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.visitors.DockerSymbolVisitor;

import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.testing.TemplateFileReader.BASE_DIR;

public class DockerVerifier {

  private DockerVerifier() {
  }

  private static final DockerParser PARSER = DockerParser.create();

  public static void verify(String fileName, IacCheck check) {
    Path path = BASE_DIR.resolve(fileName);
    Tree root = Verifier.parse(PARSER, path);
    DockerSymbolVisitor symbolVisitor = new DockerSymbolVisitor();
    symbolVisitor.scan(mock(InputFileContext.class), root);
    Verifier.verify(root, BASE_DIR.resolve(fileName), check, Verifier.TestContext::new);
  }

  public static void verifyContent(String content, IacCheck check) {
    Verifier.verify(PARSER, content, check);
  }

  public static void verifyNoIssue(String fileName, IacCheck check) {
    Path path = BASE_DIR.resolve(fileName);
    Tree root = Verifier.parse(PARSER, path);
    DockerSymbolVisitor symbolVisitor = new DockerSymbolVisitor();
    symbolVisitor.scan(mock(InputFileContext.class), root);
    Verifier.verifyNoIssue(root, path, check, Verifier.TestContext::new);
  }
}
