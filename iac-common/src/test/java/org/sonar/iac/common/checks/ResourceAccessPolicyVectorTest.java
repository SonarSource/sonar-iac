/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.common.checks;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTextTree.text;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTree.tree;

class ResourceAccessPolicyVectorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void loadJsonFile() {
    var logMessage = "Unable to load unknown.json";
    assertThrows(IOException.class, () -> ResourceAccessPolicyVector.loadJsonFile("unknown.json"),
      logMessage);

    assertThat(ResourceAccessPolicyVector.loadResourceAccessPolicies("unknown.json")).isEmpty();
    assertThat(logTester.logs(Level.WARN)).contains(logMessage);
  }

  @Test
  void isResourceAccessPolicy() {
    assertThat(ResourceAccessPolicyVector.isResourceAccessPolicy(text("backup-gateway:Backup"))).isTrue();
    assertThat(ResourceAccessPolicyVector.isResourceAccessPolicy(text("foo:bar"))).isFalse();
    assertThat(ResourceAccessPolicyVector.isResourceAccessPolicy(tree())).isFalse();
  }
}
