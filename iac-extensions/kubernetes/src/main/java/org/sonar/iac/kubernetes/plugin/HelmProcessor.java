/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.kubernetes.plugin;

import java.io.IOException;
import java.util.Map;
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
import org.sonar.iac.kubernetes.plugin.filesystem.FileSystemProvider;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import static org.sonar.iac.helm.LineNumberCommentInserter.addLineComments;
import static org.sonar.iac.helm.LineNumberCommentRemover.cleanSource;

public class HelmProcessor {
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

    if (FileSystemProvider.containsLineBreak(inputFile.filename())) {
      throw parseExceptionFor(inputFile, "File name contains line break", null);
    }

    var sourceWithComments = addLineComments(source);
    inputFileContext.setSourceWithComments(sourceWithComments);

    var relatedHelmFiles = helmFilesystem.getRelatedHelmFiles(inputFileContext);
    inputFileContext.setAdditionalFiles(relatedHelmFiles);

    var path = HelmFileSystem.getFileRelativePath(inputFileContext);
    return evaluateHelmTemplate(path, inputFileContext, sourceWithComments, relatedHelmFiles);
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
}
