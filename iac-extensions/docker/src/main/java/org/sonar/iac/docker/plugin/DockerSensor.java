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
package org.sonar.iac.docker.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.sonar.iac.docker.checks.DockerCheckList;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.visitors.DockerHighlightingVisitor;
import org.sonar.iac.docker.visitors.DockerMetricsVisitor;
import org.sonar.iac.docker.visitors.DockerSymbolVisitor;

public class DockerSensor extends IacSensor {
  private final Checks<IacCheck> checks;

  public DockerSensor(
    SonarRuntime sonarRuntime,
    FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory,
    NoSonarFilter noSonarFilter,
    DockerLanguage language) {
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language);
    checks = checkFactory.create(DockerExtension.REPOSITORY_KEY);
    checks.addAnnotatedChecks(getChecks());
  }

  protected List<Class<?>> getChecks() {
    return DockerCheckList.checks();
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .processesFilesIndependently()
      .name("IaC " + language.getName() + " Sensor");
    activateHiddenFilesProcessing(descriptor);
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext, DurationStatistics statistics) {
    // Dockerfiles are detected either by being assigned to the DockerLanguage or by the path patterns defined below.
    // Because not all Dockerfiles are matched and therefore assigned to the DockerLanguage,
    // we can't use "Plugin-RequiredForLanguages": "docker" in the manifest to optimize Plugin Downloads
    // Because of this reason we also can't use descriptor.onlyOnLanguage(DockerLanguage.KEY) for this sensor

    var fileSystem = sensorContext.fileSystem();
    FilePredicates p = fileSystem.predicates();

    Set<String> pathPatterns = new HashSet<>();

    // Because we can't add "**/Dockerfile.*" as a filenamePattern to the DockerLanguage, we need to match the files here via a path pattern
    // It's not possible to add it as a pattern because it would match files like "Dockerfile.java" which would result in a collision for the
    // Docker and Java language.
    pathPatterns.add("**/Dockerfile.*");
    pathPatterns.add("**/Dockerfile-*");
    pathPatterns.add("**/Dockerfile_*");

    // same patterns in lowercase
    pathPatterns.add("**/dockerfile.*");
    pathPatterns.add("**/dockerfile-*");
    pathPatterns.add("**/dockerfile_*");

    // In SQ-IDE Language#filenamePatterns() is not implemented, so all Dockerfiles are detected by path patterns not via the Docker language
    // Support will be implemented with SLCORE-526
    if (isSonarLintContext(sensorContext.runtime())) {
      pathPatterns.add("**/Dockerfile");
      pathPatterns.add("**/dockerfile");
      pathPatterns.add("**/**.Dockerfile");
      pathPatterns.add("**/**.dockerfile");
    }

    FilePredicate dockerLanguageOrPathPattern = p.or(
      p.hasLanguage(DockerLanguage.KEY),
      p.matchesPathPatterns(pathPatterns.toArray(new String[0])));

    if (((DockerLanguage) language).isUsingDefaultFilePattern()) {
      dockerLanguageOrPathPattern = p.and(
        // Equivalent to p.doesNotMatchPathPattern("*.j2", "*.md"), but more efficient as the scanner has an extension cache
        p.not(p.or(
          p.hasExtension("md"),
          p.hasExtension("j2"))),
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
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    visitors.add(new DockerSymbolVisitor());
    visitors.addAll(preCheckVisitors());
    visitors.add(new ChecksVisitor(checks, statistics));
    if (isNotSonarLintContext(sensorContext.runtime())) {
      visitors.add(new DockerMetricsVisitor(fileLinesContextFactory, noSonarFilter, sensorTelemetry));
      visitors.add(new DockerHighlightingVisitor());
    }
    return visitors;
  }

  protected List<TreeVisitor<InputFileContext>> preCheckVisitors() {
    return List.of();
  }

  @Override
  protected String getActivationSettingKey() {
    return DockerSettings.ACTIVATION_KEY;
  }
}
