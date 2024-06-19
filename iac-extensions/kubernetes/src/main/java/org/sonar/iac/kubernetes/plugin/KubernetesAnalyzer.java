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

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.Analyzer;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;
import org.sonar.iac.kubernetes.tree.impl.KubernetesFileTreeImpl;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import javax.annotation.Nullable;

import static org.sonar.iac.common.yaml.YamlFileUtils.splitLines;

public class KubernetesAnalyzer extends Analyzer {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesAnalyzer.class);
  private static final String DIRECTIVE_IN_COMMENT = "#.*\\{\\{";
  private static final String DIRECTIVE_IN_SINGLE_QUOTE = "'[^']*\\{\\{[^']*'";
  private static final String DIRECTIVE_IN_DOUBLE_QUOTE = "\"[^\"]*\\{\\{[^\"]*\"";
  private static final String CODEFRESH_VARIABLES = "\\$\\{\\{[\\w\\s]+}}";
  private static final Pattern HELM_DIRECTIVE_IN_COMMENT_OR_STRING = Pattern.compile("(" +
    String.join("|", DIRECTIVE_IN_COMMENT, DIRECTIVE_IN_SINGLE_QUOTE, DIRECTIVE_IN_DOUBLE_QUOTE, CODEFRESH_VARIABLES) + ")");

  private HelmProcessor helmProcessor;
  private final KubernetesParserStatistics kubernetesParserStatistics;

  public KubernetesAnalyzer(String repositoryKey, TreeParser<? extends Tree> parser, List<TreeVisitor<InputFileContext>> visitors, DurationStatistics statistics, HelmProcessor helmProcessor, KubernetesParserStatistics kubernetesParserStatistics) {
    super(repositoryKey, parser, visitors, statistics);
    this.helmProcessor = helmProcessor;
    this.kubernetesParserStatistics = kubernetesParserStatistics;
  }

  @Override
  protected InputFileContext createInputFileContext(SensorContext sensorContext, InputFile inputFile) {
    return new HelmInputFileContext(sensorContext, inputFile);
  }

  @Override
  public Tree parse(@Nullable InputFileContext inputFileContext, String content) {
    if (!hasHelmContent(content)) {
      return kubernetesParserStatistics.recordPureKubernetesFile(() -> parser.parse(content, inputFileContext));
    } else {
      return kubernetesParserStatistics.recordHelmFile(() -> parseHelmFile(content, (HelmInputFileContext) inputFileContext));
    }
  }

  public static boolean hasHelmContent(String text) {
    String[] lines = splitLines(text);
    for (String line : lines) {
      if (hasHelmContentInLine(line)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasHelmContentInLine(String line) {
    return line.contains("{{") && !HELM_DIRECTIVE_IN_COMMENT_OR_STRING.matcher(line).find();
  }

  private FileTree parseHelmFile(String source, @Nullable HelmInputFileContext inputFileContext) {
    if (inputFileContext == null) {
      LOG.debug("No InputFileContext provided, skipping processing of Helm file");
      return buildEmptyTree();
    }

    if (isInvalidHelmInputFile(inputFileContext)) {
      return buildEmptyTree();
    }

    LOG.debug("Helm content detected in file '{}'", inputFileContext.inputFile);
    if (!helmProcessor.isHelmEvaluatorInitialized()) {
      LOG.debug("Helm evaluator is not initialized, skipping processing of Helm file {}", inputFileContext.inputFile);
      return buildEmptyTree();
    }

    FileTree result;
    try {
      result = evaluateAndParseHelmFile(source, inputFileContext);
    } catch (ParseException pe) {
      var details = pe.getDetails();
      if (details != null && details.contains("\" associated with template \"aggregatingTemplate\"")) {
        LOG.debug("Helm file {} requires a named template that is missing; this feature is not yet supported, skipping processing of Helm file", inputFileContext.inputFile);
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

  private FileTree evaluateAndParseHelmFile(String source, HelmInputFileContext inputFileContext) {
    var evaluatedAndCleanedSource = helmProcessor.process(source, inputFileContext);

    if (evaluatedAndCleanedSource.isBlank()) {
      LOG.debug("Blank evaluated file, skipping processing of Helm file {}", inputFileContext.inputFile);
      return buildEmptyTree();
    }

    return KubernetesFileTreeImpl.fromFileTree(
      (FileTree) parser.parse(evaluatedAndCleanedSource, inputFileContext),
      inputFileContext.getGoTemplateTree());
  }

  private FileTree buildEmptyTree() {
    return (FileTree) parser.parse("{}", null);
  }

  static boolean isInvalidHelmInputFile(HelmInputFileContext helmFileCtx) {
    return isValuesFile(helmFileCtx) || isChartFile(helmFileCtx) || isTplFile(helmFileCtx);
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
   * Only Chart.yaml is accepted by helm command, the Chart.yml is invalid and not recognized as Chart directory
   */
  private static boolean isChartFile(HelmInputFileContext helmFileCtx) {
    var isChartYaml = "Chart.yaml".equals(helmFileCtx.inputFile.filename());
    if (isChartYaml && helmFileCtx.isInChartRootDirectory()) {
      LOG.debug("Helm Chart.yaml file detected, skipping parsing {}", helmFileCtx.inputFile);
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
}
