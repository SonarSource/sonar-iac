/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.plugin;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.analyzer.SingleFileAnalyzer;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.reports.ExternalReportWildcardProvider;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.docker.checks.DockerCheckList;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.reports.hadolint.HadolintImporter;
import org.sonar.iac.docker.visitors.DockerHighlightingVisitor;
import org.sonar.iac.docker.visitors.DockerMetricsVisitor;
import org.sonar.iac.docker.visitors.DockerSymbolVisitor;

import static org.sonar.iac.common.warnings.DefaultAnalysisWarningsWrapper.NOOP_ANALYSIS_WARNINGS;

public class DockerSensor extends IacSensor {
  private final Checks<IacCheck> checks;
  private final AnalysisWarningsWrapper analysisWarnings;
  private final HadolintRulesDefinition rulesDefinition;

  public DockerSensor(
    SonarRuntime sonarRuntime,
    HadolintRulesDefinition rulesDefinition,
    FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory,
    NoSonarFilter noSonarFilter,
    DockerLanguage language) {
    this(sonarRuntime, rulesDefinition, fileLinesContextFactory, checkFactory, noSonarFilter, language, NOOP_ANALYSIS_WARNINGS);
  }

  public DockerSensor(
    SonarRuntime sonarRuntime,
    HadolintRulesDefinition rulesDefinition,
    FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory,
    NoSonarFilter noSonarFilter,
    DockerLanguage language,
    AnalysisWarningsWrapper analysisWarnings) {
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language);
    checks = checkFactory.create(DockerExtension.REPOSITORY_KEY);
    checks.addAnnotatedChecks(DockerCheckList.checks());
    this.analysisWarnings = analysisWarnings;
    this.rulesDefinition = rulesDefinition;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .processesFilesIndependently()
      .name("IaC " + language.getName() + " Sensor");
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext, DurationStatistics statistics) {
    // Dockerfiles are detected either by being assigned to the DockerLanguage or by the path patterns defined below.
    // Because not all Dockerfiles are matched and therefore assigned to the DockerLanguage,
    // we can't use "Plugin-RequiredForLanguages": "docker" in the manifest to optimize Plugin Downloads
    // Because of this reason we also can't use descriptor.onlyOnLanguage(DockerLanguage.KEY) for this sensor

    var fileSystem = sensorContext.fileSystem();
    FilePredicates p = fileSystem.predicates();

    // Because we can't add "**/Dockerfile.*" as a filenamePattern to the DockerLanguage, we need to match the files here via a path pattern
    // It's not possible to add it as a pattern because it would match files like "Dockerfile.java" which would result in a collision for the
    // Docker and Java language.
    FilePredicate pathPatterns = p.matchesPathPattern("**/Dockerfile.*");

    // In SQ-IDE Language#filenamePatterns() is not implemented, so all Dockerfiles are detected by path patterns not via the Docker language
    // Support will be implemented with SLCORE-526
    if (!isNotSonarLintContext(sensorContext.runtime())) {
      pathPatterns = p.or(
        pathPatterns,
        p.matchesPathPattern("**/Dockerfile"),
        p.matchesPathPattern("**/**.Dockerfile"),
        p.matchesPathPattern("**/**.dockerfile"));
    }

    FilePredicate dockerLanguageOrPathPattern = p.or(
      p.hasLanguage(DockerLanguage.KEY),
      pathPatterns);

    if (((DockerLanguage) language).isUsingDefaultFilePattern()) {
      dockerLanguageOrPathPattern = p.and(
        p.doesNotMatchPathPattern("*.j2"),
        dockerLanguageOrPathPattern);
    }

    return p.and(p.hasType(InputFile.Type.MAIN), dockerLanguageOrPathPattern);
  }

  @Override
  protected SingleFileAnalyzer createAnalyzer(SensorContext sensorContext, DurationStatistics statistics) {
    return new SingleFileAnalyzer(repositoryKey(), DockerParser.create(), visitors(sensorContext, statistics), statistics);
  }

  @Override
  protected String repositoryKey() {
    return DockerExtension.REPOSITORY_KEY;
  }

  @Override
  protected void importExternalReports(SensorContext sensorContext) {
    ExternalReportWildcardProvider.getReportFiles(sensorContext, DockerSettings.HADOLINT_REPORTS_KEY)
      .forEach(report -> new HadolintImporter(sensorContext, rulesDefinition, analysisWarnings).importReport(report));
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    visitors.add(new DockerSymbolVisitor());
    visitors.add(new ChecksVisitor(checks, statistics));
    if (isNotSonarLintContext(sensorContext.runtime())) {
      visitors.add(new DockerMetricsVisitor(fileLinesContextFactory, noSonarFilter, sensorTelemetry));
      visitors.add(new DockerHighlightingVisitor());
    }
    return visitors;
  }

  @Override
  protected String getActivationSettingKey() {
    return DockerSettings.ACTIVATION_KEY;
  }
}
