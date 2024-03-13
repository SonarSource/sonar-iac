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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.YamlSensor;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.kubernetes.checks.KubernetesCheckList;
import org.sonar.iac.kubernetes.visitors.AdjustableChecksVisitor;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.KubernetesHighlightingVisitor;
import org.sonar.iac.kubernetes.visitors.LocationShifter;
import org.sonar.iac.kubernetes.visitors.SecondaryLocationLocator;

public class KubernetesSensor extends YamlSensor {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesSensor.class);
  private static final String HELM_ACTIVATION_KEY = "sonar.kubernetes.internal.helm.enable";
  private final HelmEvaluator helmEvaluator;

  private HelmProcessor helmProcessor;
  private final LocationShifter locationShifter = new LocationShifter();
  private final SecondaryLocationLocator secondaryLocationLocator = new SecondaryLocationLocator(new YamlParser());
  private final KubernetesParserStatistics kubernetesParserStatistics = new KubernetesParserStatistics();

  public KubernetesSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, KubernetesLanguage language, HelmEvaluator helmEvaluator) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, KubernetesCheckList.checks());
    this.helmEvaluator = helmEvaluator;
  }

  @Override
  protected void initContext(SensorContext sensorContext) {
    if (shouldEnableHelmAnalysis(sensorContext) && helmProcessor == null) {
      LOG.debug("Initializing Helm processor");
      var helmFileSystem = new HelmFileSystem(sensorContext.fileSystem());
      helmProcessor = new HelmProcessor(helmEvaluator, helmFileSystem);
    } else {
      LOG.debug("Skipping initialization of Helm processor");
    }
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(YAML_LANGUAGE_KEY)
      .name("IaC " + language.getName() + " Sensor");

    // Note: KubernetesSensor shouldn't call `descriptor.processesFilesIndependently()` or `super.describe(descriptor)`,
    // otherwise Helm analysis won't receive all the files needed for template evaluation in PR analysis.
  }

  @Override
  protected TreeParser<Tree> treeParser() {
    return new KubernetesParser(helmProcessor, locationShifter, kubernetesParserStatistics);
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    if (isNotSonarLintContext(sensorContext)) {
      visitors.add(new KubernetesHighlightingVisitor());
      visitors.add(new YamlMetricsVisitor(fileLinesContextFactory, noSonarFilter));
    }
    visitors.add(new AdjustableChecksVisitor(checks, statistics, locationShifter, secondaryLocationLocator));
    return visitors;
  }

  @Override
  protected String repositoryKey() {
    return KubernetesExtension.REPOSITORY_KEY;
  }

  @Override
  protected String getActivationSettingKey() {
    return KubernetesSettings.ACTIVATION_KEY;
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    return predicates.and(predicates.hasType(InputFile.Type.MAIN),
      customFilePredicate(sensorContext));
  }

  @Override
  protected FilePredicate customFilePredicate(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    return predicates.or(yamlK8sOrHelmFilePredicate(sensorContext), tplHelmFilePredicate(sensorContext));
  }

  private static FilePredicate yamlK8sOrHelmFilePredicate(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    var helmTemplatePredicate = predicates.and(
      predicates.matchesPathPattern("**/templates/**"),
      new HelmProjectMemberPredicate(sensorContext));
    var valuesYamlOrChartYamlPredicate = predicates.and(
      predicates.matchesPathPatterns(new String[] {"**/values.yaml", "**/values.yml", "**/Chart.yaml"}),
      new HelmProjectMemberPredicate(sensorContext));
    return predicates.and(
      predicates.hasLanguage(YAML_LANGUAGE_KEY),
      predicates.or(
        new KubernetesFilePredicate(),
        helmTemplatePredicate,
        valuesYamlOrChartYamlPredicate));
  }

  private static FilePredicate tplHelmFilePredicate(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    return predicates.and(
      predicates.matchesPathPattern("**/templates/*.tpl"),
      new HelmProjectMemberPredicate(sensorContext));
  }

  @Override
  protected void afterExecute() {
    kubernetesParserStatistics.logStatistics();
  }

  @Override
  protected InputFileContext createInputFileContext(SensorContext sensorContext, InputFile inputFile) {
    return new HelmInputFileContext(sensorContext, inputFile);
  }

  private boolean shouldEnableHelmAnalysis(SensorContext sensorContext) {
    var isNotSonarLintContext = isNotSonarLintContext(sensorContext);
    boolean isHelmAnalysisEnabled = sensorContext.config().getBoolean(HELM_ACTIVATION_KEY).orElse(true);
    var isHelmEvaluatorExecutableAvailable = HelmProcessor.isHelmEvaluatorExecutableAvailable();
    LOG.debug("Checking conditions for enabling Helm analysis: isNotSonarLintContext={}, isHelmActivationFlagTrue={}, isHelmEvaluatorExecutableAvailable={}",
      isNotSonarLintContext, isHelmAnalysisEnabled, isHelmEvaluatorExecutableAvailable);
    if (isNotSonarLintContext && isHelmAnalysisEnabled && !isHelmEvaluatorExecutableAvailable) {
      LOG.info("Helm analysis is not supported for the current platform");
    }
    return isNotSonarLintContext && isHelmAnalysisEnabled && isHelmEvaluatorExecutableAvailable;
  }

  static class KubernetesFilePredicate implements FilePredicate {

    private static final Pattern LINE_TERMINATOR = Pattern.compile("[\\n\\r\\u2028\\u2029]");

    // https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/#required-fields
    private static final Set<String> IDENTIFIER = Set.of("apiVersion", "kind", "metadata");
    private static final Logger LOG = LoggerFactory.getLogger(KubernetesFilePredicate.class);
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @Override
    public boolean apply(InputFile inputFile) {
      return hasKubernetesObjectStructure(inputFile);
    }

    private static boolean hasKubernetesObjectStructure(InputFile inputFile) {
      var identifierCount = 0;
      var hasExpectedIdentifier = false;
      try (var bufferedInputStream = new BufferedInputStream(inputFile.inputStream())) {
        // Only firs 8k bytes is read to avoid slow execution for big one-line files
        byte[] bytes = bufferedInputStream.readNBytes(DEFAULT_BUFFER_SIZE);
        var text = new String(bytes, inputFile.charset());
        String[] lines = LINE_TERMINATOR.split(text);
        for (String line : lines) {
          if (IDENTIFIER.stream().anyMatch(line::startsWith)) {
            identifierCount++;
          } else if (FILE_SEPARATOR.equals(line)) {
            identifierCount = 0;
          }
          if (identifierCount == IDENTIFIER.size()) {
            hasExpectedIdentifier = true;
          }
        }
      } catch (IOException e) {
        LOG.error("Unable to read file: {}.", inputFile);
        LOG.error(e.getMessage());
      }

      if (hasExpectedIdentifier) {
        return true;
      } else {
        LOG.debug("File without Kubernetes identifier: {}", inputFile);
        return false;
      }
    }
  }

  void setHelmProcessorForTesting(HelmProcessor helmProcessor) {
    this.helmProcessor = helmProcessor;
  }

  static class HelmProjectMemberPredicate implements FilePredicate {
    private final SensorContext sensorContext;

    HelmProjectMemberPredicate(SensorContext sensorContext) {
      this.sensorContext = sensorContext;
    }

    @Override
    public boolean apply(InputFile inputFile) {
      return HelmFileSystem.retrieveHelmProjectFolder(Path.of(inputFile.uri()), sensorContext.fileSystem()) != null;
    }
  }
}
