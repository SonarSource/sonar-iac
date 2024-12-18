/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.kubernetes.visitors;

import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;

public interface KubernetesCheckContext extends CheckContext {
  boolean shouldReportSecondaryInValues();

  /**
   * In case a rule is reporting an issue on a data that originate from a values.yaml file, calling this method and passing
   * {@code true} will allow to automatically add a secondary location to the related values in the values.yaml file.
   * This should not be enabled for rules that are security hotspots, because current SonarQube UI is not displaying them properly. They are
   * showed at the correct text range location but in the primary file, which is incorrect and misleading.
   * @param shouldReport {@code true} to enable secondary locations in values.yaml, {@code false} otherwise.
   */
  void setShouldReportSecondaryInValues(boolean shouldReport);

  void reportIssueNoLineShift(TextRange toHighlight, String message);

  InputFileContext inputFileContext();

  ProjectContext projectContext();
}
