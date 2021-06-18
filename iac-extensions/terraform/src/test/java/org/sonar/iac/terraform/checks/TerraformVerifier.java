/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.terraform.parser.HclParser;

public class TerraformVerifier {

  private TerraformVerifier() {

  }

  private static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");
  private static final HclParser PARSER = new HclParser();

  public static void verify(String fileName, IacCheck check) {
    Verifier.verify(PARSER, BASE_DIR.resolve(fileName), check);
  }
}
