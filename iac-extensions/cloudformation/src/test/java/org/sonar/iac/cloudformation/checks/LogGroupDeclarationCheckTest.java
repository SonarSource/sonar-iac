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

import org.junit.jupiter.api.Test;

import static org.sonar.iac.common.testing.Verifier.issue;

class LogGroupDeclarationCheckTest {

  @Test
  void shouldFindIssuesInYaml() {
    CloudformationVerifier.verify("LogGroupDeclarationCheck/LogGroupDeclarationCheck.yaml", new LogGroupDeclarationCheck());
  }

  @Test
  void shouldFindIssuesInJson() {
    CloudformationVerifier.verify("LogGroupDeclarationCheck/LogGroupDeclarationCheck.json", new LogGroupDeclarationCheck(),
      issue(5, 14, 5, 37, "Make sure missing \"Log Groups\" declaration is intended here."),
      issue(8, 14, 8, 41),
      issue(11, 14, 11, 38),
      issue(14, 14, 14, 39),
      issue(101, 14, 101, 37),
      issue(132, 14, 132, 37),
      issue(167, 14, 167, 38),
      issue(186, 14, 186, 39),
      issue(244, 14, 244, 41));
  }

}
