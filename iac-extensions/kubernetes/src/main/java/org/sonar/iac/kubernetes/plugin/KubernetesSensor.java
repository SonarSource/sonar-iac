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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
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
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.analyzer.Analyzer;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.YamlSensor;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.kubernetes.checks.KubernetesCheckList;
import org.sonar.iac.kubernetes.plugin.filesystem.DefaultFileSystemProvider;
import org.sonar.iac.kubernetes.plugin.filesystem.FileSystemProvider;
import org.sonar.iac.kubernetes.plugin.predicates.KubernetesOrHelmFilePredicate;
import org.sonar.iac.kubernetes.visitors.KubernetesChecksVisitor;
import org.sonar.iac.kubernetes.visitors.KubernetesHighlightingVisitor;
import org.sonar.iac.kubernetes.visitors.ProjectContext;
import org.sonar.iac.kubernetes.visitors.ProjectContextEnricherVisitor;

public class KubernetesSensor extends YamlSensor {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesSensor.class);
  private static final String HELM_ACTIVATION_KEY = "sonar.kubernetes.internal.helm.enable";
  private final HelmEvaluator helmEvaluator;
  @Nullable
  private final SonarLintFileListener sonarLintFileListener;
  private final ProjectContext projectContext = new ProjectContext();
  private HelmProcessor helmProcessor;
  private FileSystemProvider fileSystemProvider;
  private final KubernetesParserStatistics kubernetesParserStatistics = new KubernetesParserStatistics();

  public KubernetesSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, KubernetesLanguage language, HelmEvaluator helmEvaluator) {
    this(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, helmEvaluator, null);
  }

  // Constructor for SonarLint
  public KubernetesSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, KubernetesLanguage language, HelmEvaluator helmEvaluator, @Nullable SonarLintFileListener sonarLintFileListener) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, KubernetesCheckList.checks());
    this.helmEvaluator = helmEvaluator;
    this.sonarLintFileListener = sonarLintFileListener;
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
  protected void initContext(SensorContext sensorContext) {
    if (shouldEnableHelmAnalysis(sensorContext) && helmProcessor == null) {
      LOG.debug("Initializing Helm processor");
      fileSystemProvider = new DefaultFileSystemProvider(sensorContext.fileSystem());
      var helmFileSystem = new HelmFileSystem(fileSystemProvider);
      helmProcessor = new HelmProcessor(helmEvaluator, helmFileSystem);
      helmProcessor.initialize();
    } else {
      LOG.debug("Skipping initialization of Helm processor");
    }
    if (sonarLintFileListener != null) {
      var statistics = new DurationStatistics(sensorContext.config());
      var analyzer = createAnalyzerForUpdatingProjectContext(statistics);
      sonarLintFileListener.initContext(sensorContext, analyzer, projectContext);
    }
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    if (isNotSonarLintContext(sensorContext)) {
      visitors.add(new KubernetesHighlightingVisitor());
      visitors.add(new YamlMetricsVisitor(fileLinesContextFactory, noSonarFilter));
    }
    visitors.add(new ProjectContextEnricherVisitor(projectContext));
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
  protected Analyzer createAnalyzer(SensorContext sensorContext, DurationStatistics statistics) {
    return new KubernetesAnalyzer(
      repositoryKey(),
      new YamlParser(),
      visitors(sensorContext, statistics),
      statistics,
      new HelmParser(helmProcessor),
      kubernetesParserStatistics,
      new KubernetesChecksVisitor(checks, statistics, projectContext, fileSystemProvider));
  }

  /**
   * It creates a {@link KubernetesAnalyzer} used for updating {@link ProjectContext} when files changes in SonarLint.
   * The difference between this one and created by {@link KubernetesSensor#createAnalyzer(SensorContext, DurationStatistics)}
   * is that this one uses only {@link ProjectContextEnricherVisitor}.
   */
  private KubernetesAnalyzer createAnalyzerForUpdatingProjectContext(DurationStatistics statistics) {
    return new KubernetesAnalyzer(
      repositoryKey(),
      new YamlParser(),
      List.of(new ProjectContextEnricherVisitor(projectContext)),
      statistics,
      new HelmParser(helmProcessor),
      kubernetesParserStatistics,
      new EmptyChecksVisitor());
  }

  void setHelmProcessorForTesting(HelmProcessor helmProcessor) {
    this.helmProcessor = helmProcessor;
  }

  private static boolean shouldEnableHelmAnalysis(SensorContext sensorContext) {
    var isNotSonarLintContext = IacSensor.isNotSonarLintContext(sensorContext);
    boolean isHelmAnalysisEnabled = sensorContext.config().getBoolean(HELM_ACTIVATION_KEY).orElse(true);
    var isHelmEvaluatorExecutableAvailable = HelmProcessor.isHelmEvaluatorExecutableAvailable();
    LOG.debug("Checking conditions for enabling Helm analysis: isNotSonarLintContext={}, isHelmActivationFlagTrue={}, isHelmEvaluatorExecutableAvailable={}",
      isNotSonarLintContext, isHelmAnalysisEnabled, isHelmEvaluatorExecutableAvailable);
    if (isNotSonarLintContext && isHelmAnalysisEnabled && !isHelmEvaluatorExecutableAvailable) {
      LOG.info("Helm analysis is not supported for the current platform");
    }
    return isNotSonarLintContext && isHelmAnalysisEnabled && isHelmEvaluatorExecutableAvailable;
  }
}
