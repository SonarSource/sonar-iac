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
