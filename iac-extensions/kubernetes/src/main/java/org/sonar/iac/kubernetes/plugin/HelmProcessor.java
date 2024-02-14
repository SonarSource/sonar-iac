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
import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputPath;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.HelmFilesystem;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;
import org.sonar.iac.helm.utils.OperatingSystemUtils;
import org.sonarsource.api.sonarlint.SonarLintSide;

import static org.sonar.iac.helm.LineNumberCommentInserter.addLineComments;

@ScannerSide
@SonarLintSide
@ExtensionPoint
public class HelmProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(HelmProcessor.class);
  private HelmEvaluator helmEvaluator;
  private final Map<String, GoTemplateTree> inputFileToGoAst = new HashMap<>();

  public HelmProcessor(HelmEvaluator helmEvaluator) {
    this.helmEvaluator = helmEvaluator;
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

  @CheckForNull
  String processHelmTemplate(String path, String source, InputFileContext inputFileContext) {
    if (!isHelmEvaluatorInitialized()) {
      throw new IllegalStateException("Attempt to process Helm template with uninitialized Helm evaluator");
    }
    if (source.isBlank()) {
      LOG.debug("The file {} is blank, skipping evaluation", inputFileContext.inputFile);
      return null;
    }

    // TODO: better support of Helm project structure
    var sourceWithComments = addLineComments(source);
    Map<String, InputFile> additionalFiles = HelmFilesystem.getRelatedHelmFiles(inputFileContext);
    var fileContents = validateAndReadFiles(inputFileContext.inputFile, additionalFiles);
    return evaluateHelmTemplate(path, inputFileContext.inputFile, sourceWithComments, fileContents);
  }

  @CheckForNull
  public GoTemplateTree getGoAstForInputFile(InputPath inputPath) {
    // We are not caching the result of the evaluation, that's why its `remove` instead of `get`.
    return inputFileToGoAst.remove(inputPath.uri().toString());
  }

  static Map<String, String> validateAndReadFiles(InputFile inputFile, Map<String, InputFile> files) {
    // Currently we are only looking for the default location of the values file
    if (!files.containsKey("values.yaml") && !files.containsKey("values.yml")) {
      throw parseExceptionFor(inputFile, "Failed to find values file", null);
    }

    Map<String, String> fileContents = new HashMap<>(files.size());

    for (Map.Entry<String, InputFile> filenameToInputFile : files.entrySet()) {
      var additionalInputFile = filenameToInputFile.getValue();
      String fileContent;
      try {
        fileContent = additionalInputFile.contents();
      } catch (IOException e) {
        throw parseExceptionFor(inputFile, "Failed to read file at " + additionalInputFile, e.getMessage());
      }

      fileContents.put(filenameToInputFile.getKey(), fileContent);
    }
    return fileContents;
  }

  String evaluateHelmTemplate(String path, InputFile inputFile, String content, Map<String, String> templateDependencies) {
    try {
      var templateEvaluationResult = helmEvaluator.evaluateTemplate(path, content, templateDependencies);
      inputFileToGoAst.put(inputFile.uri().toString(), GoTemplateTreeImpl.fromPbTree(templateEvaluationResult.getAst()));
      return templateEvaluationResult.getTemplate();
    } catch (IllegalStateException | IOException e) {
      throw parseExceptionFor(inputFile, "Template evaluation failed", e.getMessage());
    }
  }

  private static ParseException parseExceptionFor(InputFile inputFile, String cause, @Nullable String details) {
    return new ParseException("Failed to evaluate Helm file " + inputFile + ": " + cause, null, details);
  }
}
