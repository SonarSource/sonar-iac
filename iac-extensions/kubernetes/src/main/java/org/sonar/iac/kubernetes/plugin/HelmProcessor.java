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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;
import org.sonar.iac.helm.utils.OperatingSystemUtils;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import static org.sonar.iac.helm.LineNumberCommentInserter.addLineComments;
import static org.sonar.iac.helm.LineNumberCommentRemover.cleanSource;

public class HelmProcessor {
  public static final List<String> LINE_SEPARATORS = List.of("\n", "\r\n", "\r", "\u2028", "\u2029");
  private static final Logger LOG = LoggerFactory.getLogger(HelmProcessor.class);
  private final HelmEvaluator helmEvaluator;
  private final HelmFileSystem helmFilesystem;
  private boolean isEvaluatorInitialized;

  public HelmProcessor(HelmEvaluator helmEvaluator, HelmFileSystem helmFilesystem) {
    this.helmEvaluator = helmEvaluator;
    this.helmFilesystem = helmFilesystem;
  }

  public static boolean isHelmEvaluatorExecutableAvailable() {
    return OperatingSystemUtils.getCurrentPlatformIfSupported().isPresent();
  }

  public void initialize() {
    try {
      helmEvaluator.initialize();
      isEvaluatorInitialized = true;
    } catch (IOException e) {
      LOG.debug("Failed to initialize Helm evaluator, analysis of Helm files will be disabled", e);
    }
  }

  public boolean isHelmEvaluatorInitialized() {
    return isEvaluatorInitialized;
  }

  public String process(String source, HelmInputFileContext inputFileContext) {
    LocationShifter.readLinesSizes(source, inputFileContext);
    var evaluatedSource = processHelmTemplate(source, inputFileContext);
    if (evaluatedSource != null) {
      return cleanSource(evaluatedSource, inputFileContext);
    }
    return "";
  }

  @CheckForNull
  String processHelmTemplate(String source, HelmInputFileContext inputFileContext) {
    if (!isHelmEvaluatorInitialized()) {
      throw new IllegalStateException("Attempt to process Helm template with uninitialized Helm evaluator");
    }
    if (inputFileContext.getHelmProjectDirectory() == null) {
      throw new ParseException("Failed to evaluate Helm file " + inputFileContext.inputFile + ": Failed to resolve Helm project " +
        "directory", null, null);
    }

    var inputFile = inputFileContext.inputFile;
    if (source.isBlank()) {
      LOG.debug("The file {} is blank, skipping evaluation", inputFile);
      return null;
    }

    var sourceWithComments = addLineComments(source);
    inputFileContext.setSourceWithComments(sourceWithComments);
    inputFileContext.setAdditionalFiles(helmFilesystem.getRelatedHelmFiles(inputFileContext));
    var fileContents = validateAndReadFiles(inputFileContext);
    var path = helmFilesystem.getFileRelativePath(inputFileContext);
    return evaluateHelmTemplate(path, inputFileContext, sourceWithComments, fileContents);
  }

  static Map<String, String> validateAndReadFiles(HelmInputFileContext inputFileContext) {
    if (containsLineBreak(inputFileContext.inputFile.filename())) {
      throw parseExceptionFor(inputFileContext.inputFile, "File name contains line break", null);
    } else if (inputFileContext.getAdditionalFiles().keySet().stream().anyMatch(HelmProcessor::containsLineBreak)) {
      inputFileContext.setAdditionalFiles(
        inputFileContext.getAdditionalFiles().entrySet().stream()
          .filter(entry -> !containsLineBreak(entry.getKey()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
      LOG.debug("Some additional files have names containing line breaks, skipping them");
    }

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

  String evaluateHelmTemplate(String path, HelmInputFileContext inputFileContext, String sourceWithComments, Map<String, String> templateDependencies) {
    var inputFile = inputFileContext.inputFile;
    try {
      var templateEvaluationResult = helmEvaluator.evaluateTemplate(path, sourceWithComments, templateDependencies);
      inputFileContext.setGoTemplateTree(GoTemplateTreeImpl.fromPbTree(templateEvaluationResult.getAst(), sourceWithComments));
      return templateEvaluationResult.getTemplate();
    } catch (IllegalStateException | IOException e) {
      throw parseExceptionFor(inputFile, "Template evaluation failed", e.getMessage());
    }
  }

  private static ParseException parseExceptionFor(InputFile inputFile, String cause, @Nullable String details) {
    return new ParseException("Failed to evaluate Helm file " + inputFile + ": " + cause, null, details);
  }

  private static boolean containsLineBreak(String filename) {
    return LINE_SEPARATORS.stream().anyMatch(filename::contains);
  }
}
