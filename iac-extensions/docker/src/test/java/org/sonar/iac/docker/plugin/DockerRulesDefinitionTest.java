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
package org.sonar.iac.docker.plugin;

import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.extension.IacRulesDefinition;
import org.sonar.iac.common.testing.AbstractRulesDefinitionTest;
import org.sonar.iac.docker.checks.DockerCheckList;

class DockerRulesDefinitionTest extends AbstractRulesDefinitionTest {

  @Override
  protected Version sonarVersion() {
    return Version.create(9, 3);
  }

  @Override
  protected IacRulesDefinition getRulesDefinition(SonarRuntime sonarRuntime) {
    return new DockerRulesDefinition(sonarRuntime);
  }

  @Override
  protected String languageKey() {
    return "docker";
  }

  @Override
  protected List<Class<?>> checks() {
    return DockerCheckList.checks();
  }
}
