/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.plugin;

import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.api.utils.Version;
import org.sonar.iac.arm.checks.ArmCheckList;
import org.sonar.iac.common.extension.IacRulesDefinition;
import org.sonar.iac.common.testing.AbstractRulesDefinitionTest;

class ArmRulesDefinitionTest extends AbstractRulesDefinitionTest {

  @Override
  protected Version sonarVersion() {
    return Version.create(10, 0);
  }

  @Override
  protected IacRulesDefinition getRulesDefinition(SonarRuntime sonarRuntime) {
    return new ArmRulesDefinition(sonarRuntime);
  }

  @Override
  protected String languageKey() {
    return "azureresourcemanager";
  }

  @Override
  protected List<Class<?>> checks() {
    return ArmCheckList.checks();
  }
}
