/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
    verify(path, check);
  }

  private static void verify(Path path, IacCheck check) {
    Tree root = Verifier.parse(PARSER, path);
    DockerSymbolVisitor symbolVisitor = new DockerSymbolVisitor();
    symbolVisitor.scan(mock(InputFileContext.class), root);
    Verifier.verify(root, path, check, Verifier.TestContext::new);
  }

  public static void verifyContent(String content, IacCheck check) {
    Path path = Verifier.contentToTmp(content).toPath();
    verify(path, check);
  }

  public static void verifyNoIssue(String fileName, IacCheck check) {
    Path path = BASE_DIR.resolve(fileName);
    verifyNoIssue(path, check);
  }

  public static void verifyNoIssue(Path path, IacCheck check) {
    Tree root = Verifier.parse(PARSER, path);
    DockerSymbolVisitor symbolVisitor = new DockerSymbolVisitor();
    symbolVisitor.scan(mock(InputFileContext.class), root);
    Verifier.verifyNoIssue(root, path, check, Verifier.TestContext::new);
  }

  public static void verifyContentNoIssue(String content, IacCheck check) {
    Path path = Verifier.contentToTmp(content).toPath();
    verifyNoIssue(path, check);
  }
}
