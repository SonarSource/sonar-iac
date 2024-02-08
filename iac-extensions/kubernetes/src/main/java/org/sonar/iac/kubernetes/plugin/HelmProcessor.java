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
package org.sonar.iac.kubernetes.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
import org.sonar.iac.helm.utils.OperatingSystemUtils;
import org.sonar.iac.kubernetes.visitors.LocationShifter;
import org.sonarsource.api.sonarlint.SonarLintSide;

import static org.sonar.iac.helm.LineNumberCommentInserter.addLineComments;
import static org.sonar.iac.helm.LineNumberCommentRemover.cleanSource;
import static org.sonar.iac.helm.utils.HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory;

@ScannerSide
@SonarLintSide
@ExtensionPoint
public class HelmProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(HelmProcessor.class);
  private HelmEvaluator helmEvaluator;

  private LocationShifter locationShifter;

  public HelmProcessor(HelmEvaluator helmEvaluator, LocationShifter locationShifter) {
    this.helmEvaluator = helmEvaluator;
    this.locationShifter = locationShifter;
  }

  public static boolean isHelmEvaluatorExecutableAvailable() {
    return OperatingSystemUtils.getCurrentPlatformIfSupported().isPresent();
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

  String processHelmTemplate(String fileRelativePath, String source, InputFileContext inputFileContext) {
    locationShifter.readLinesSizes(source, inputFileContext);
    if (!isHelmEvaluatorInitialized()) {
      throw new IllegalStateException("Attempt to process Helm template with uninitialized Helm evaluator");
    }
    if (source.isBlank()) {
      LOG.debug("The file {} is blank, skipping evaluation", inputFileContext.inputFile);
      return source;
    }

    // TODO: better support of Helm project structure
    var sourceWithComments = addLineComments(source);
    Map<String, InputFile> additionalFiles = additionalFilesOfHelmProjectDirectory(inputFileContext);
    var fileContents = HelmPreprocessor.preProcess(inputFileContext, additionalFiles);

    // Evaluate the template
    var evaluatedSource = evaluateHelmTemplate(fileRelativePath, inputFileContext.inputFile, sourceWithComments, fileContents);

    // Postprocess the file
    return HelmPostprocessor.postProcess(evaluatedSource, inputFileContext, locationShifter);
  }

  private String evaluateHelmTemplate(String path, InputFile inputFile, String content, Map<String, String> templateDependencies) {
    try {
      var evaluationResult = helmEvaluator.evaluateTemplate(path, content, templateDependencies);
      return evaluationResult.getTemplate();
    } catch (IllegalStateException | IOException e) {
      throw parseExceptionFor(inputFile, "Template evaluation failed", e.getMessage());
    }
  }

  private static ParseException parseExceptionFor(InputFile inputFile, String cause, @Nullable String details) {
    return new ParseException("Failed to evaluate Helm file " + inputFile + ": " + cause, null, details);
  }

  static class HelmPostprocessor {
    static String postProcess(String evaluatedSource, InputFileContext inputFileContext, LocationShifter locationShifter) {
      return cleanSource(evaluatedSource, inputFileContext, locationShifter);
    }
  }
}
