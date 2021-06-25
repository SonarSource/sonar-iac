/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TerraformCheckListTest {

  /**
   * Enforces that each check is declared in the list.
   */
  @Test
  void count() {
    int count = 0;
    List<File> files = new ArrayList<>();
    for (String folder : new String[] { "src/main/java/org/sonar/iac/terraform/checks/"}) {
      files.addAll(Arrays.asList(new File(folder).listFiles((f, name) -> name.endsWith("java"))));
    }
    for (File file : files) {
      if (file.getName().endsWith("Check.java") && !file.getName().startsWith("Abstract")) {
        count++;
      }
    }
    assertThat(TerraformCheckList.checks()).hasSize(count);
  }

  /**
   * Enforces that each check has a test
   */
  @Test
  void test() {
    List<Class<?>> checks = TerraformCheckList.checks();
    for (Class<?> cls : checks) {
      if (cls != ParsingErrorCheck.class) {
        String testName = '/' + cls.getName().replace('.', '/') + "Test.class";
        assertThat(getClass().getResource(testName))
          .overridingErrorMessage("No test for " + cls.getSimpleName())
          .isNotNull();
      }
    }
  }
}
