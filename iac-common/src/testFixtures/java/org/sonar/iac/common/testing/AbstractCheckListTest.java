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
package org.sonar.iac.common.testing;

import java.io.File;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.checks.ParsingErrorCheck;
import org.sonar.iac.common.checks.ToDoCommentCheck;

import static org.apache.commons.io.filefilter.FileFilterUtils.and;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.prefixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class AbstractCheckListTest {

  protected abstract List<Class<?>> checks();

  protected abstract File checkClassDir();

  protected boolean hasTodoCommentCheck() {
    return true;
  }

  protected boolean hasParsingFailureCheck() {
    return true;
  }

  @Test
  void containsParsingErrorCheck() {
    assumeTrue(hasParsingFailureCheck());
    assertThat(checks()).contains(ParsingErrorCheck.class);
  }

  @Test
  void containsToDoCommentCheck() {
    assumeTrue(hasTodoCommentCheck());
    assertThat(checks()).contains(ToDoCommentCheck.class);
  }

  /**
   * Enforces that each check is declared in the list.
   */
  @Test
  protected void count() {
    IOFileFilter filter = and(suffixFileFilter("Check.java"), notFileFilter(prefixFileFilter("Abstract")));
    Collection<File> files = FileUtils.listFiles(checkClassDir(), filter, trueFileFilter());
    // We can increase the files size by 2 because the ParsingErrorCheck and ToDoCommentCheck are located in iac-commons
    int checksSize = files.size();
    if (hasTodoCommentCheck()) {
      checksSize++;
    }
    if (hasParsingFailureCheck()) {
      checksSize++;
    }
    assertThat(checks()).hasSize(checksSize);
  }

  /**
   * Enforces that each check has a test
   */
  @Test
  protected void test() {
    List<Class<?>> checks = checks();
    for (Class<?> cls : checks) {
      // Exception on class from the common package
      if (!cls.getName().contains("iac.common.checks")) {
        String testName = getCheckTestResourcePath(cls);
        assertThat(getClass().getResource(testName))
          .overridingErrorMessage("No test for " + cls.getSimpleName())
          .isNotNull();
      }
    }
  }

  protected String getCheckTestResourcePath(Class<?> checkClass) {
    return '/' + checkClass.getName().replace('.', '/') + "Test.class";
  }
}
