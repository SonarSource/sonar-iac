/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.SonarRuntimeUtils;
import org.sonar.iac.common.extension.analyzer.Analyzer;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.predicates.GithubActionsFilePredicate;
import org.sonar.iac.common.predicates.KubernetesOrHelmFilePredicate;
import org.sonar.iac.common.yaml.AbstractYamlLanguageSensor;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.kubernetes.checks.KubernetesCheckList;
import org.sonar.iac.kubernetes.plugin.filesystem.DefaultFileSystemProvider;
import org.sonar.iac.kubernetes.plugin.filesystem.FileSystemProvider;
import org.sonar.iac.kubernetes.plugin.filesystem.SonarLintFileSystemProvider;
import org.sonar.iac.kubernetes.visitors.KubernetesChecksVisitor;
import org.sonar.iac.kubernetes.visitors.KubernetesHighlightingVisitor;
import org.sonar.iac.kubernetes.visitors.ProjectContextEnricherVisitor;
import org.sonar.iac.kubernetes.visitors.ProjectContextImpl;

@DependsUpon("KustomizationSensor")
public class KubernetesSensor extends AbstractYamlLanguageSensor {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesSensor.class);
  private final HelmEvaluator helmEvaluator;
  @Nullable
  private final SonarLintFileListener sonarLintFileListener;
  private final KustomizationInfoProvider kustomizationInfoProvider;
  private final ProjectContextImpl projectContextImpl;
  private HelmProcessor helmProcessor;
  private final KubernetesParserStatistics kubernetesParserStatistics = new KubernetesParserStatistics();

  public KubernetesSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, KubernetesLanguage language, HelmEvaluator helmEvaluator, KustomizationInfoProvider kustomizationInfoProvider) {
    this(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, helmEvaluator, null, kustomizationInfoProvider);
  }

  // Constructor for SonarLint
  public KubernetesSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, KubernetesLanguage language, HelmEvaluator helmEvaluator, @Nullable SonarLintFileListener sonarLintFileListener,
    KustomizationInfoProvider kustomizationInfoProvider) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, KubernetesCheckList.checks());
    this.helmEvaluator = helmEvaluator;
    this.sonarLintFileListener = sonarLintFileListener;
    this.kustomizationInfoProvider = kustomizationInfoProvider;
    if (sonarLintFileListener != null) {
      projectContextImpl = sonarLintFileListener.getProjectContext();
    } else {
      projectContextImpl = new ProjectContextImpl();
    }
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(YAML_LANGUAGE_KEY, language.getKey())
      .name("IaC " + language.getName() + " Sensor");

    // Note: KubernetesSensor shouldn't call `descriptor.processesFilesIndependently()` or `super.describe(descriptor)`,
    // otherwise Helm analysis won't receive all the files needed for template evaluation in PR analysis.
  }

  @Override
  protected void initContext(SensorContext sensorContext) {
    projectContextImpl.setKustomizationReferencedFiles(kustomizationInfoProvider.kustomizationReferencedFiles());
    LOG.debug("Kubernetes sensor initialized with {} kustomization referenced files: {}",
      kustomizationInfoProvider.kustomizationReferencedFiles().size(), kustomizationInfoProvider.kustomizationReferencedFiles());
    var fileSystemProvider = createFileSystemProvider(sensorContext);
    if (shouldEnableHelmAnalysis(sensorContext) && helmProcessor == null) {
      LOG.debug("Initializing Helm processor");
      var helmFileSystem = new HelmFileSystem(fileSystemProvider);
      helmProcessor = new HelmProcessor(helmEvaluator, helmFileSystem);
      helmProcessor.initialize();
    } else {
      LOG.debug("Skipping initialization of Helm processor");
    }
    if (sonarLintFileListener != null) {
      sonarLintFileListener.initContext(sensorContext, helmProcessor);
    }
  }

  private FileSystemProvider createFileSystemProvider(SensorContext sensorContext) {
    if (sonarLintFileListener != null) {
      return new SonarLintFileSystemProvider(sonarLintFileListener);
    }
    return new DefaultFileSystemProvider(sensorContext.fileSystem());
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    if (SonarRuntimeUtils.isNotSonarLintContext(sensorContext.runtime())) {
      visitors.add(new KubernetesHighlightingVisitor());
      visitors.add(new YamlMetricsVisitor(fileLinesContextFactory, noSonarFilter, sensorTelemetry));
    }
    visitors.add(new ProjectContextEnricherVisitor(projectContextImpl));
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
  protected FilePredicate mainFilePredicate(SensorContext sensorContext, DurationStatistics statistics) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    return predicates.and(predicates.hasType(InputFile.Type.MAIN),
      customFilePredicate(sensorContext, statistics));
  }

  @Override
  protected FilePredicate customFilePredicate(SensorContext sensorContext, DurationStatistics statistics) {
    var predicates = sensorContext.fileSystem().predicates();
    var githubActionsFilePredicate = new GithubActionsFilePredicate(predicates, isExtendedLoggingEnabled(sensorContext),
      statistics.timer("KubernetesNotGithubActionsFilePredicate"));
    var kubernetesOrHelmFilePredicate = new KubernetesOrHelmFilePredicate(sensorContext, isExtendedLoggingEnabled(sensorContext),
      statistics.timer("KubernetesOrHelmFilePredicate"));
    return predicates.and(
      predicates.not(githubActionsFilePredicate),
      kubernetesOrHelmFilePredicate);
  }

  @Override
  protected void afterExecute(SensorContext sensorContext) {
    super.afterExecute(sensorContext);
    kubernetesParserStatistics.storeTelemetry(sensorContext);
    kubernetesParserStatistics.logStatistics();
  }

  @Override
  protected Analyzer createAnalyzer(SensorContext sensorContext, DurationStatistics statistics) {
    return KubernetesAnalyzerFactory.createAnalyzer(
      visitors(sensorContext, statistics),
      statistics,
      helmProcessor,
      kubernetesParserStatistics,
      new KubernetesChecksVisitor(checks, statistics, projectContextImpl),
      sonarLintFileListener);
  }

  void setHelmProcessorForTesting(HelmProcessor helmProcessor) {
    this.helmProcessor = helmProcessor;
  }

  private static boolean shouldEnableHelmAnalysis(SensorContext sensorContext) {
    boolean isHelmAnalysisEnabled = sensorContext.config().getBoolean(KubernetesSettings.HELM_ACTIVATION_KEY).orElse(true);
    var isHelmEvaluatorExecutableAvailable = HelmProcessor.isHelmEvaluatorExecutableAvailable();
    LOG.debug("Checking conditions for enabling Helm analysis; Activated Helm analysis:{}, Helm supported for this platform:{}",
      isHelmAnalysisEnabled, isHelmEvaluatorExecutableAvailable);
    if (isHelmAnalysisEnabled && !isHelmEvaluatorExecutableAvailable) {
      LOG.info("Helm analysis is not supported for the current platform");
    }
    return isHelmAnalysisEnabled && isHelmEvaluatorExecutableAvailable;
  }
}
