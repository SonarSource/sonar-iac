/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.AbstractCheckListTest;

class TerraformCheckListTest extends AbstractCheckListTest {

  @Override
  protected List<Class<?>> checks() {
    return TerraformCheckList.checks();
  }

  @Override
  protected File checkClassDir() {
    return new File("src/main/java/org/sonar/iac/terraform/checks/");
  }

  @TestFactory
  Stream<DynamicTest> lifecycleBlocksDoNotRaiseIssues() {
    return TerraformCheckList.checks().stream()
      .map(checkClass -> DynamicTest.dynamicTest(checkClass.getSimpleName(), () -> {
        IacCheck check = (IacCheck) checkClass.getDeclaredConstructor().newInstance();
        TerraformVerifier.verifyNoIssue("TerraformCheckList/lifecycleBlocks.tf", check);
      }));
  }
}
