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
package org.sonar.iac.jvmframeworkconfig.checks.micronaut;

import java.io.File;
import java.util.List;
import org.sonar.iac.common.testing.AbstractCheckListTest;

class MicronautConfigCheckListTest extends AbstractCheckListTest {
  @Override
  protected List<Class<?>> checks() {
    return MicronautConfigCheckList.checks();
  }

  @Override
  protected File checkClassDir() {
    return new File("src/main/java/org/sonar/iac/jvmframeworkconfig/checks/micronaut/");
  }

  @Override
  protected boolean hasTodoCommentCheck() {
    return false;
  }

  @Override
  protected boolean hasParsingFailureCheck() {
    return false;
  }
}
