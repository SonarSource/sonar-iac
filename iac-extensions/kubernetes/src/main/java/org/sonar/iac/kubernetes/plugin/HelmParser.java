package org.sonar.iac.kubernetes.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;
import org.sonar.iac.kubernetes.tree.impl.KubernetesFileTreeImpl;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import javax.annotation.Nullable;

public class HelmParser {
  private static final Logger LOG = LoggerFactory.getLogger(HelmParser.class);
  private YamlParser parser = new YamlParser();
  @Nullable
  private final HelmProcessor helmProcessor;

  public HelmParser(@Nullable HelmProcessor helmProcessor) {
    this.helmProcessor = helmProcessor;
  }

  public FileTree parseHelmFile(String source, @Nullable HelmInputFileContext inputFileContext) {
    if (inputFileContext == null) {
      LOG.debug("No InputFileContext provided, skipping processing of Helm file");
      return buildEmptyTree();
    }

    if (isInvalidHelmInputFile(inputFileContext)) {
      return buildEmptyTree();
    }

    LOG.debug("Helm content detected in file '{}'", inputFileContext.inputFile);
    if (helmProcessor == null || !helmProcessor.isHelmEvaluatorInitialized()) {
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
      parser.parse(evaluatedAndCleanedSource, inputFileContext),
      inputFileContext.getGoTemplateTree());
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

  private FileTree buildEmptyTree() {
    return parser.parse("{}", null);
  }
}
