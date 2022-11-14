/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.cloudformation.checks;

import java.io.File;
import java.util.List;
import org.sonar.iac.common.testing.AbstractCheckListTest;

class CloudformationCheckListTest extends AbstractCheckListTest {

  @Override
  protected List<Class<?>> checks() {
    return CloudformationCheckList.checks();
  }

  @Override
  protected File checkClassDir() {
    return new File("src/main/java/org/sonar/iac/cloudformation/checks/");
  }

}
