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
package org.sonar.iac.arm.plugin;

import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.iac.arm.checks.ArmCheckList;
import org.sonar.iac.common.extension.IacRulesDefinition;

public class ArmRulesDefinition extends IacRulesDefinition {

  public ArmRulesDefinition(SonarRuntime runtime) {
    super(runtime);
  }

  @Override
  public String languageKey() {
    return ArmLanguage.KEY;
  }

  @Override
  protected List<Class<?>> checks() {
    return ArmCheckList.checks();
  }
}
