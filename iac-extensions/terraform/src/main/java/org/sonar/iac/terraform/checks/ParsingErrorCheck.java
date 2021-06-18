/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

/**
 * This class does nothing. It exists only to be present in the SonarQube profile and GUI.
 * Issues for this class are created upfront, during the parsing.
 */
@Rule(key = "S2260")
public class ParsingErrorCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    // errors are reported in InputFileContext#reportParseError
  }
}
