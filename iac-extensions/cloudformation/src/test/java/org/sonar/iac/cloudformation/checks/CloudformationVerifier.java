/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.cloudformation.checks;

import org.sonar.iac.cloudformation.parser.CloudformationParser;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.testing.TemplateFileReader.BASE_DIR;

public class CloudformationVerifier {

  private CloudformationVerifier() {

  }

  private static final CloudformationParser PARSER = new CloudformationParser();

  public static void verify(String fileName, IacCheck check) {
    Verifier.verify(PARSER, BASE_DIR.resolve(fileName), check);
  }

  public static void verify(String fileName, IacCheck check, Verifier.Issue... expectedIssues) {
    Verifier.verify(PARSER, BASE_DIR.resolve(fileName), check, expectedIssues);
  }

  public static void verifyNoIssue(String fileName, IacCheck check) {
    Verifier.verifyNoIssue(PARSER, BASE_DIR.resolve(fileName), check);
  }
}
