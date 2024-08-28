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
package org.sonar.iac.common.reports;

import org.sonar.api.SonarRuntime;

public class AbstractExternalRulesDefinitionTest extends org.sonar.iac.common.testing.AbstractExternalRulesDefinitionTest {

  @Override
  public void noOpRulesDefinitionShouldNotDefineAnyRule() {
    // We don't need this test here, as we don't define noOpRulesDefinition for the TestExternalRulesDefinition
  }

  @Override
  protected AbstractExternalRulesDefinition rulesDefinition(SonarRuntime sonarRuntime) {
    return new TestExternalRulesDefinition(sonarRuntime);
  }

  @Override
  protected int numberOfRules() {
    return 1;
  }

  @Override
  protected String reportName() {
    return "testName";
  }

  @Override
  protected String reportKey() {
    return "testKey";
  }

  @Override
  protected String language() {
    return "testLanguage";
  }

  @Override
  protected AbstractExternalRulesDefinition noOpRulesDefinition() {
    return null;
  }
}

class TestExternalRulesDefinition extends AbstractExternalRulesDefinition {
  protected TestExternalRulesDefinition(SonarRuntime sonarRuntime) {
    super(sonarRuntime, "testKey", "testName", "testLanguage");
  }
}
