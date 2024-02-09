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
package org.sonar.iac.helm.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.sonar.api.impl.utils.DefaultTempFolder;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;

public class GoAstCreator {
  private final HelmEvaluator helmEvaluator;

  public GoAstCreator(File workingDir) throws IOException {
    this.helmEvaluator = new HelmEvaluator(new DefaultTempFolder(workingDir, false));
    this.helmEvaluator.initialize();
  }

  public GoTemplateTree goAstFromSource(String source, String valuesFileContent, String chartFileContent) throws IOException {
    var templateDependencies = Map.of("values.yaml", valuesFileContent, "Chart.yaml", chartFileContent);
    var evaluationResult = helmEvaluator.evaluateTemplate("templates/test.yaml", source, templateDependencies);

    return GoTemplateTreeImpl.fromPbTree(evaluationResult.getAst());
  }
}
