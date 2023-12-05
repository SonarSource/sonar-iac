/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.common.checks;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.jupiter.api.Test;

import static org.apache.commons.io.filefilter.FileFilterUtils.and;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.prefixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;
import static org.assertj.core.api.Assertions.assertThat;

class CommonChecksTest {
  private final String packageCheck = "org.sonar.iac.common.checks";

  private List<Class<?>> getListCommonChecks() {
    IOFileFilter filter = and(suffixFileFilter("Check.java"), notFileFilter(prefixFileFilter("Abstract")));
    Collection<File> files = FileUtils.listFiles(new File("src/main/java/" + packageCheck.replaceAll("[.]", "/")), filter, trueFileFilter());
    return files.stream()
      .map(file -> getNameWithoutExtension(file.getName()))
      .map(this::classForName)
      .collect(Collectors.toList());
  }

  public static String getNameWithoutExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf('.');
    return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
  }

  public Class<?> classForName(String name) {
    try {
      return Class.forName(packageCheck + "." + name);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Enforces that each check common check has a test
   */
  @Test
  void commonChecksShouldHaveTest() {
    for (Class<?> cls : getListCommonChecks()) {
      // Exception on ParsingErrorCheck that doesn't have a test class
      if (cls != ParsingErrorCheck.class) {
        String testName = '/' + cls.getName().replace('.', '/') + "Test.class";
        assertThat(getClass().getResource(testName))
          .overridingErrorMessage("No test for " + cls.getSimpleName())
          .isNotNull();
      }
    }
  }
}
