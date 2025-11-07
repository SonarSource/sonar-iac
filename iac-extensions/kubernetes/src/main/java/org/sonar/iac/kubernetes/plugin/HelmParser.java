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

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;
import org.sonar.iac.kubernetes.tree.impl.HelmFileTreeImpl;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

public class HelmParser {
  private static final Logger LOG = LoggerFactory.getLogger(HelmParser.class);
  private final YamlParser parser = new YamlParser();
  @Nullable
  private final HelmProcessor helmProcessor;

  public HelmParser(@Nullable HelmProcessor helmProcessor) {
    this.helmProcessor = helmProcessor;
  }

  public FileTree parseHelmFile(String source, HelmInputFileContext inputFileContext) {
    if (!shouldProcess(inputFileContext)) {
      return buildEmptyTree();
    }

    FileTree result;
    try {
      result = evaluateAndParseHelmFile(source, inputFileContext);
    } catch (ParseException pe) {
      var details = pe.getDetails();
      if (details != null && details.contains("\" associated with template \"aggregatingTemplate\"")) {
        LOG.debug("Helm file {} requires a named template that is missing; this feature is not yet supported, skipping processing of Helm" +
          " file", inputFileContext.inputFile);
        result = buildEmptyTree();
      } else {
        throw pe;
      }
    } catch (MarkedYamlEngineException e) {
      var exception = LocationShifter.shiftMarkedYamlException(inputFileContext, e);
      if (exception instanceof ShiftedMarkedYamlEngineException shiftedMarkedException) {
        LOG.debug("Shifting YAML exception {}", shiftedMarkedException.describeShifting());
      }
      throw exception;
    }
    return result;
  }

  private boolean shouldProcess(HelmInputFileContext inputFileContext) {
    if (isInvalidHelmInputFile(inputFileContext)) {
      return false;
    }

    if (helmProcessor == null || !helmProcessor.isHelmEvaluatorInitialized()) {
      return false;
    }
    LOG.debug("Helm content detected in file '{}'", inputFileContext.inputFile);
    return true;
  }

  private FileTree evaluateAndParseHelmFile(String source, HelmInputFileContext inputFileContext) {
    var evaluatedAndCleanedSource = helmProcessor.process(source, inputFileContext);

    if (evaluatedAndCleanedSource.isBlank()) {
      LOG.debug("Blank evaluated file, skipping processing of Helm file {}", inputFileContext.inputFile);
      return buildEmptyTree();
    }

    return HelmFileTreeImpl.fromFileTree(
      parser.parse(evaluatedAndCleanedSource, inputFileContext),
      inputFileContext.getGoTemplateTree());
  }

  static boolean isInvalidHelmInputFile(HelmInputFileContext helmFileCtx) {
    return isValuesFile(helmFileCtx) || isTplFile(helmFileCtx);
  }

  /**
   * Values files are not analyzed directly. Their value will be processed when the actual Helm chart file is evaluated and analyzed.
   */
  private static boolean isValuesFile(HelmInputFileContext helmFileCtx) {
    var filename = helmFileCtx.inputFile.filename();
    var isValuesYaml = "values.yaml".equals(filename) || "values.yml".equals(filename);
    if (isValuesYaml && helmFileCtx.isInChartRootDirectory()) {
      LOG.debug("Helm values file detected, skipping parsing {}", helmFileCtx.inputFile);
      return true;
    }
    return false;
  }

  /**
   * Tpl files are not analyzed directly. Their value will be processed when the actual Helm chart file is evaluated and analyzed.
   */
  private static boolean isTplFile(HelmInputFileContext helmFileCtx) {
    if (helmFileCtx.inputFile.filename().endsWith(".tpl")) {
      LOG.debug("Helm tpl file detected, skipping parsing {}", helmFileCtx.inputFile);
      return true;
    }
    return false;
  }

  private FileTree buildEmptyTree() {
    return parser.parse("{}", null);
  }
}
