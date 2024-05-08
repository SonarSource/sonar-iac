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
import java.util.ArrayList;
import java.util.List;
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
import org.sonar.iac.common.checks.Trilean;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.YamlSensor;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.kubernetes.checks.KubernetesCheckList;
import org.sonar.iac.kubernetes.plugin.predicates.KubernetesOrHelmFilePredicate;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.KubernetesChecksVisitor;
import org.sonar.iac.kubernetes.visitors.KubernetesHighlightingVisitor;
import org.sonar.iac.kubernetes.visitors.ProjectContext;

public class KubernetesSensor extends YamlSensor {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesSensor.class);
  private static final String HELM_ACTIVATION_KEY = "sonar.kubernetes.internal.helm.enable";
  private final HelmEvaluator helmEvaluator;

  final ProjectContext.Builder projectContextBuilder = ProjectContext.builder();

  private HelmProcessor helmProcessor;
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
      helmProcessor.initialize();
    } else {
      LOG.debug("Skipping initialization of Helm processor");
    }
  }

  void checkExistingLimitRange(List<InputFile> inputFiles) {
    for (InputFile inputFile : inputFiles) {
      try {
        if (inputFile.contents().contains("LimitRange")) {
          LOG.debug("LimitRange detected, related rules will be suppressed");
          projectContextBuilder.setLimitRange(Trilean.TRUE);
          return;
        }
      } catch (IOException e) {
        LOG.debug("IOException while detecting LimitRange, related rules will be suppressed");
        projectContextBuilder.setLimitRange(Trilean.UNKNOWN);
        return;
      }
    }
  }

  @Override
  protected List<InputFile> inputFiles(SensorContext sensorContext) {
    var inputFiles = super.inputFiles(sensorContext);
    checkExistingLimitRange(inputFiles);
    return inputFiles;
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
  protected TreeParser<? extends Tree> treeParser() {
    return new KubernetesParser(helmProcessor, kubernetesParserStatistics);
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    if (isNotSonarLintContext(sensorContext)) {
      visitors.add(new KubernetesHighlightingVisitor());
      visitors.add(new YamlMetricsVisitor(fileLinesContextFactory, noSonarFilter));
    }
    visitors.add(new KubernetesChecksVisitor(checks, statistics, projectContextBuilder.build()));
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
    return new KubernetesOrHelmFilePredicate(sensorContext);
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

  void setHelmProcessorForTesting(HelmProcessor helmProcessor) {
    this.helmProcessor = helmProcessor;
  }

}
