/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.Arrays;
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
import org.sonar.iac.common.extension.IacProjectSensor;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.SonarRuntimeUtils;
import org.sonar.iac.common.extension.analyzer.SingleFileAnalyzer;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.docker.checks.CombinedTagAndDigestCheck;
import org.sonar.iac.docker.checks.DockerCheckList;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.visitors.DockerHighlightingVisitor;
import org.sonar.iac.docker.visitors.DockerMetricsVisitor;
import org.sonar.iac.docker.visitors.DockerSymbolVisitor;

public class DockerSensor extends IacSensor {
  private static final Set<String> BOT_CONFIG_PATHS = Set.of(
    ".github/dependabot.yml",
    ".github/dependabot.yaml",
    "renovate.json",
    "renovate.jsonc",
    "renovate.json5",
    ".github/renovate.json",
    ".github/renovate.jsonc",
    ".github/renovate.json5",
    ".gitlab/renovate.json",
    ".gitlab/renovate.jsonc",
    ".gitlab/renovate.json5",
    ".renovaterc",
    ".renovaterc.json",
    ".renovaterc.jsonc",
    ".renovaterc.json5");

  // Entries without a wildcard, so the two stay in sync automatically.
  private static final List<String> DOCKERFILE_NAMES = Arrays.stream(DockerSettings.DEFAULT_FILE_PATTERNS.split(","))
    .filter(pattern -> !pattern.contains("*"))
    .toList();

  protected final Checks<IacCheck> checks;

  public DockerSensor(
    SonarRuntime sonarRuntime,
    FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory,
    NoSonarFilter noSonarFilter,
    DockerLanguage language,
    IacProjectSensor projectSensor) {
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language, projectSensor);
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
      .name("IaC " + languageName() + " Sensor");
    SonarRuntimeUtils.activateHiddenFilesProcessing(sonarRuntime, descriptor);
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
    for (var name : DOCKERFILE_NAMES) {
      pathPatterns.add("**/" + name + ".*");
      pathPatterns.add("**/" + name + "-*");
      pathPatterns.add("**/" + name + "_*");
    }

    // In SQ-IDE Language#filenamePatterns() is not implemented, so all Dockerfiles are detected by path patterns not via the Docker language
    // Support will be implemented with SLCORE-526
    if (SonarRuntimeUtils.isSonarLintContext(sensorContext.runtime())) {
      for (var name : DOCKERFILE_NAMES) {
        pathPatterns.add("**/" + name);
        pathPatterns.add("**/**." + name);
      }
    }

    FilePredicate dockerLanguageOrPathPattern = p.or(
      p.hasLanguage(DockerLanguage.KEY),
      p.matchesPathPatterns(pathPatterns.toArray(new String[0])));

    if (((DockerLanguage) language).isUsingDefaultFilePattern()) {
      dockerLanguageOrPathPattern = p.and(
        // Equivalent to p.doesNotMatchPathPattern("*.j2", "*.md"), but more efficient as the scanner has an extension cache
        p.not(p.or(
          p.hasExtension("md"),
          p.hasExtension("j2"),
          // will match on Jenkinsfile and jenkinsfile
          p.matchesPathPattern("*enkinsfile"))),
        dockerLanguageOrPathPattern);
    }

    return p.and(p.hasType(InputFile.Type.MAIN), dockerLanguageOrPathPattern);
  }

  @Override
  protected SingleFileAnalyzer createAnalyzer(SensorContext sensorContext, DurationStatistics statistics) {
    return new SingleFileAnalyzer(repositoryKey(), DockerParser.create(), visitors(sensorContext, statistics), statistics, sensorTelemetry);
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
    visitors.add(createChecksVisitor(activeChecks(sensorContext), statistics));
    if (SonarRuntimeUtils.isNotSonarLintContext(sensorContext.runtime())) {
      visitors.add(new DockerMetricsVisitor(fileLinesContextFactory, noSonarFilter, sensorTelemetry));
      visitors.add(new DockerHighlightingVisitor());
    }
    return visitors;
  }

  protected ChecksVisitor createChecksVisitor(List<ChecksVisitor.ActiveCheck> activeChecks, DurationStatistics statistics) {
    return new ChecksVisitor(activeChecks, statistics);
  }

  private List<ChecksVisitor.ActiveCheck> activeChecks(SensorContext sensorContext) {
    var activeChecks = ChecksVisitor.activeChecks(checks);
    var s8431Active = activeChecks.stream()
      .map(ChecksVisitor.ActiveCheck::check)
      .anyMatch(CombinedTagAndDigestCheck.class::isInstance);
    if (!s8431Active || !isUsingDependencyManagementBot(sensorContext)) {
      return activeChecks;
    }
    return activeChecks.stream()
      .filter(activeCheck -> !(activeCheck.check() instanceof CombinedTagAndDigestCheck))
      .toList();
  }

  private static boolean isUsingDependencyManagementBot(SensorContext sensorContext) {
    if (SonarRuntimeUtils.isSonarLintContext(sensorContext.runtime())) {
      // hasRelativePath throws in SQ-IDE, and its filesystem only holds the
      // files under analysis, so a bot config would never be indexed there.
      return false;
    }
    var fileSystem = sensorContext.fileSystem();
    var predicates = fileSystem.predicates();
    return BOT_CONFIG_PATHS.stream()
      .anyMatch(path -> fileSystem.hasFiles(predicates.hasRelativePath(path)));
  }

  protected List<TreeVisitor<InputFileContext>> preCheckVisitors() {
    return List.of();
  }

  @Override
  protected String getActivationSettingKey() {
    return DockerSettings.ACTIVATION_KEY;
  }
}
