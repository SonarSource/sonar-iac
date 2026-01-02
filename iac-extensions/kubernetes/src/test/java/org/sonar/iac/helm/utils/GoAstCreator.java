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
    this.helmEvaluator.start();
  }

  public GoTemplateTree goAstFromSource(String source, String valuesFileContent, String chartFileContent) throws IOException {
    var templateDependencies = Map.of("values.yaml", valuesFileContent, "Chart.yaml", chartFileContent);
    var evaluationResult = helmEvaluator.evaluateTemplate("templates/test.yaml", source, templateDependencies);

    return GoTemplateTreeImpl.fromPbTree(evaluationResult.getAst(), source);
  }
}
