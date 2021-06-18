/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.sonar.iac.cloudformation.parser.CloudformationParser;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.Verifier;

public class CloudformationVerifier {

  private CloudformationVerifier() {

  }

  private static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");
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
