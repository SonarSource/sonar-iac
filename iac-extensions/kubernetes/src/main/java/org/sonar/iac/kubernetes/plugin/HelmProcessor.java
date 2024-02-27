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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;
import org.sonar.iac.helm.utils.OperatingSystemUtils;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

import static org.sonar.iac.helm.LineNumberCommentInserter.addLineComments;

public class HelmProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(HelmProcessor.class);
  private final HelmEvaluator helmEvaluator;
  private final HelmFileSystem helmFilesystem;
  private boolean isEvaluatorInitialized = true;

  public HelmProcessor(HelmEvaluator helmEvaluator, SensorContext sensorContext) {
    this.helmEvaluator = helmEvaluator;
    this.helmFilesystem = new HelmFileSystem(sensorContext.fileSystem());
    initialize();
  }

  public static boolean isHelmEvaluatorExecutableAvailable() {
    return OperatingSystemUtils.getCurrentPlatformIfSupported().isPresent();
  }

  private void initialize() {
    try {
      helmEvaluator.initialize();
    } catch (IOException e) {
      LOG.debug("Failed to initialize Helm evaluator, analysis of Helm files will be disabled", e);
      isEvaluatorInitialized = false;
    }
  }

  public boolean isHelmEvaluatorInitialized() {
    return isEvaluatorInitialized;
  }

  @CheckForNull
  String processHelmTemplate(String path, String source, HelmInputFileContext inputFileContext) {
    if (!isHelmEvaluatorInitialized()) {
      throw new IllegalStateException("Attempt to process Helm template with uninitialized Helm evaluator");
    }

    var inputFile = inputFileContext.inputFile;
    if (source.isBlank()) {
      LOG.debug("The file {} is blank, skipping evaluation", inputFile);
      return null;
    }

    var sourceWithComments = addLineComments(source);
    inputFileContext.setSourceWithComments(sourceWithComments);
    inputFileContext.setAdditionalFiles(helmFilesystem.getRelatedHelmFiles(inputFileContext.inputFile));
    var fileContents = validateAndReadFiles(inputFileContext);
    return evaluateHelmTemplate(path, inputFileContext, sourceWithComments, fileContents);
  }

  static Map<String, String> validateAndReadFiles(HelmInputFileContext inputFileContext) {
    // Currently we are only looking for the default location of the values file
    if (!inputFileContext.hasAdditionalFile("values.yaml") && !inputFileContext.hasAdditionalFile("values.yml")) {
      throw parseExceptionFor(inputFileContext.inputFile, "Failed to find values file", null);
    }

    var files = inputFileContext.getAdditionalFiles();
    Map<String, String> fileContents = new HashMap<>(files.size());

    for (Map.Entry<String, InputFile> filenameToInputFile : files.entrySet()) {
      var additionalInputFile = filenameToInputFile.getValue();
      String fileContent;
      try {
        fileContent = additionalInputFile.contents();
      } catch (IOException e) {
        throw parseExceptionFor(inputFileContext.inputFile, "Failed to read file at " + additionalInputFile, e.getMessage());
      }

      fileContents.put(filenameToInputFile.getKey(), fileContent);
    }
    return fileContents;
  }

  String evaluateHelmTemplate(String path, HelmInputFileContext inputFileContext, String content, Map<String, String> templateDependencies) {
    var inputFile = inputFileContext.inputFile;
    try {
      var templateEvaluationResult = helmEvaluator.evaluateTemplate(path, content, templateDependencies);
      inputFileContext.setGoTemplateTree(GoTemplateTreeImpl.fromPbTree(templateEvaluationResult.getAst()));
      return templateEvaluationResult.getTemplate();
    } catch (IllegalStateException | IOException e) {
      throw parseExceptionFor(inputFile, "Template evaluation failed", e.getMessage());
    }
  }

  private static ParseException parseExceptionFor(InputFile inputFile, String cause, @Nullable String details) {
    return new ParseException("Failed to evaluate Helm file " + inputFile + ": " + cause, null, details);
  }
}
