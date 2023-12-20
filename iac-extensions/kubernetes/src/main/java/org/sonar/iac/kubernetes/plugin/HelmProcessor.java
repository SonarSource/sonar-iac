/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import java.io.IOException;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonarsource.api.sonarlint.SonarLintSide;

import static org.sonar.iac.helm.utils.HelmFilesystemUtils.findValuesFile;

@ScannerSide
@SonarLintSide
@ExtensionPoint
public class HelmProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(HelmProcessor.class);
  private HelmEvaluator helmEvaluator;

  public HelmProcessor(HelmEvaluator helmEvaluator) {
    this.helmEvaluator = helmEvaluator;
  }

  public void initialize() {
    try {
      helmEvaluator.initialize();
    } catch (IOException e) {
      LOG.debug("Failed to initialize Helm evaluator, analysis of Helm files will be disabled", e);
      this.helmEvaluator = null;
    }
  }

  public boolean isHelmEvaluatorInitialized() {
    return helmEvaluator != null;
  }

  String processHelmTemplate(String filename, String source, InputFileContext inputFileContext) {
    if (helmEvaluator == null) {
      throw new IllegalStateException("Attempt to process Helm template with uninitialized Helm evaluator");
    }

    // TODO: better support of Helm project structure
    var sourceWithComments = HelmPreprocessor.addLineComments(source);
    var valuesFile = findValuesFile(inputFileContext);
    var valuesFileContent = validateAndReadValuesFile(valuesFile, inputFileContext.inputFile);
    return evaluateHelmTemplate(filename, inputFileContext.inputFile, sourceWithComments, valuesFileContent);
  }

  private static String validateAndReadValuesFile(@Nullable InputFile valuesFile, InputFile inputFile) {
    if (valuesFile == null) {
      throw new ParseException("Failed to find values file, skipping processing of Helm file " + inputFile,
        inputFile.newPointer(0, 0), null);
    }

    String valuesFileContent;
    try {
      valuesFileContent = valuesFile.contents();
    } catch (IOException e) {
      throw new ParseException("Failed to read values file at " + valuesFile + " while evaluating Helm file " + inputFile,
        inputFile.newPointer(0, 0), e.getMessage());
    }

    if (valuesFileContent.isBlank()) {
      throw new ParseException("Values file at " + valuesFile + " is empty, skipping processing of Helm file " + inputFile,
        inputFile.newPointer(0, 0), null);
    }

    return valuesFileContent;
  }

  private String evaluateHelmTemplate(String path, InputFile inputFile, String content, String valuesFileContent) {
    try {
      var evaluationResult = helmEvaluator.evaluateTemplate(path, content, valuesFileContent);
      return evaluationResult.getTemplate();
    } catch (IllegalStateException | IOException e) {
      throw new ParseException("Template evaluation failed, skipping processing of Helm file " + path, inputFile.newPointer(0, 0), e.getMessage());
    }
  }
}
