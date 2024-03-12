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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;
import org.sonar.iac.kubernetes.tree.impl.KubernetesFileTreeImpl;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.sonar.iac.common.yaml.YamlFileUtils.splitLines;
import static org.sonar.iac.helm.LineNumberCommentRemover.cleanSource;

public class KubernetesParser extends YamlParser {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesParser.class);

  private static final String DIRECTIVE_IN_COMMENT = "#.*\\{\\{";
  private static final String DIRECTIVE_IN_SINGLE_QUOTE = "'[^']*\\{\\{[^']*'";
  private static final String DIRECTIVE_IN_DOUBLE_QUOTE = "\"[^\"]*\\{\\{[^\"]*\"";
  private static final String CODEFRESH_VARIABLES = "\\$\\{\\{[\\w\\s]+}}";
  private static final Pattern HELM_DIRECTIVE_IN_COMMENT_OR_STRING = Pattern.compile("(" +
    String.join("|", DIRECTIVE_IN_COMMENT, DIRECTIVE_IN_SINGLE_QUOTE, DIRECTIVE_IN_DOUBLE_QUOTE, CODEFRESH_VARIABLES) + ")");

  private final HelmProcessor helmProcessor;
  private final LocationShifter locationShifter;
  private final KubernetesParserStatistics kubernetesParserStatistics;

  public KubernetesParser(HelmProcessor helmProcessor, LocationShifter locationShifter, KubernetesParserStatistics kubernetesParserStatistics) {
    this.helmProcessor = helmProcessor;
    this.locationShifter = locationShifter;
    this.kubernetesParserStatistics = kubernetesParserStatistics;
  }

  @Override
  public FileTree parse(String source, @Nullable InputFileContext inputFileContext) {
    if (!hasHelmContent(source)) {
      return kubernetesParserStatistics.recordPureKubernetesFile(() -> super.parse(source, inputFileContext));
    } else {
      return kubernetesParserStatistics.recordHelmFile(() -> parseHelmFile(source, (HelmInputFileContext) inputFileContext));
    }
  }

  private FileTree parseHelmFile(String source, @Nullable HelmInputFileContext inputFileContext) {
    var defaultFileTree = validateInputFileContext(inputFileContext);
    if (defaultFileTree.isPresent()) {
      return defaultFileTree.get();
    }

    LOG.debug("Helm content detected in file '{}'", inputFileContext.inputFile);
    if (!helmProcessor.isHelmEvaluatorInitialized()) {
      LOG.debug("Helm evaluator is not initialized, skipping processing of Helm file {}", inputFileContext.inputFile);
      return super.parse("{}", null, FileTree.Template.HELM);
    }

    FileTree result;
    try {
      result = evaluateAndParseHelmFile(source, inputFileContext);
    } catch (ParseException pe) {
      var details = pe.getDetails();
      if (details != null && details.contains("\" associated with template \"aggregatingTemplate\"")) {
        LOG.debug("Helm file {} requires a named template that is missing; this feature is not yet supported, skipping processing of Helm file", inputFileContext.inputFile);
        result = super.parse("{}", null, FileTree.Template.HELM);
      } else {
        throw pe;
      }
    } catch (MarkedYamlEngineException e) {
      var exception = locationShifter.shiftMarkedYamlException(inputFileContext, e);
      if (exception instanceof ShiftedMarkedYamlEngineException shiftedMarkedException) {
        LOG.debug("Shifting YAML exception {}", shiftedMarkedException.describeShifting());
      }
      throw exception;
    }
    return result;
  }

  private Optional<FileTree> validateInputFileContext(@Nullable HelmInputFileContext inputFileContext) {
    if (inputFileContext == null) {
      LOG.debug("No InputFileContext provided, skipping processing of Helm file");
      return buildEmptyTree(null);
    }

    var isValuesYaml = "values.yaml".equals(inputFileContext.inputFile.filename()) ||
      "values.yml".equals(inputFileContext.inputFile.filename());
    var isInChartRootDirectory = isInChartRootDirectory(inputFileContext);
    if (isValuesYaml && isInChartRootDirectory) {
      LOG.debug("Helm values file detected, skipping parsing {}", inputFileContext.inputFile);
      return buildEmptyTree(inputFileContext);
    }

    // only Chart.yaml is accepted by helm command, the Chart.yml is invalid and not recognized as Chart directory
    var isChartYaml = "Chart.yaml".equals(inputFileContext.inputFile.filename());
    if (isChartYaml && isInChartRootDirectory) {
      LOG.debug("Helm Chart.yaml file detected, skipping parsing {}", inputFileContext.inputFile);
      return buildEmptyTree(inputFileContext);
    }

    if (inputFileContext.inputFile.filename().endsWith(".tpl")) {
      LOG.debug("Helm tpl file detected, skipping parsing {}", inputFileContext.inputFile);
      return buildEmptyTree(inputFileContext);
    }

    return Optional.empty();
  }

  private Optional<FileTree> buildEmptyTree(@Nullable HelmInputFileContext inputFileContext) {
    return Optional.ofNullable(super.parse("{}", inputFileContext, FileTree.Template.HELM));
  }

  private boolean isInChartRootDirectory(HelmInputFileContext inputFileContext) {
    var rootChartDirectory = helmProcessor.getHelmFilesystem().retrieveHelmProjectFolder(Path.of(inputFileContext.inputFile.uri()));
    return inputFileContext.inputFile.path().getParent() != null && inputFileContext.inputFile.path().getParent().equals(rootChartDirectory);
  }

  private FileTree evaluateAndParseHelmFile(String source, HelmInputFileContext inputFileContext) {
    locationShifter.readLinesSizes(source, inputFileContext);

    var evaluatedSource = helmProcessor.processHelmTemplate(source, inputFileContext);
    var evaluatedAndCleanedSource = Optional.ofNullable(evaluatedSource)
      .map(template -> cleanSource(template, inputFileContext, locationShifter))
      .orElse("");
    if (evaluatedAndCleanedSource.isBlank()) {
      LOG.debug("Blank evaluated file, skipping processing of Helm file {}", inputFileContext.inputFile);
      return super.parse("{}", null, FileTree.Template.HELM);
    }

    return KubernetesFileTreeImpl.fromFileTree(
      super.parse(evaluatedAndCleanedSource, inputFileContext, FileTree.Template.HELM),
      inputFileContext.getGoTemplateTree());
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
}
