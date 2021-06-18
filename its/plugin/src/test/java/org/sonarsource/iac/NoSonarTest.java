/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonarsource.iac;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NoSonarTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/nosonar/";
  private static final String NO_SONAR_PROFILE_NAME = "nosonar-profile";
  private static final String RULE_KEY = "S6273";

  @Test
  public void test_terraform_nosonar() {
    checkForLanguage("terraformNoSonar", "terraform");
  }

  private void checkForLanguage(String projectKey, String language) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, language, NO_SONAR_PROFILE_NAME));

    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(1);
    assertThat(getIssuesForRule(projectKey, language + ":" + RULE_KEY)).hasSize(1);
  }
}
