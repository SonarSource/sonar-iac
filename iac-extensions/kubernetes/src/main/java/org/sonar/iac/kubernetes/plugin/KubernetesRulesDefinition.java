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
package org.sonar.iac.kubernetes.plugin;

import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.iac.common.extension.IacRulesDefinition;
import org.sonar.iac.kubernetes.checks.KubernetesCheckList;

public class KubernetesRulesDefinition extends IacRulesDefinition {

  public KubernetesRulesDefinition(SonarRuntime runtime) {
    super(runtime);
  }

  @Override
  protected List<Class<?>> checks() {
    return KubernetesCheckList.checks();
  }

  @Override
  public String languageKey() {
    return KubernetesLanguage.KEY;
  }
}
