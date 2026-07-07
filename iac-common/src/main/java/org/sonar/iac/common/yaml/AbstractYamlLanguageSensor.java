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
package org.sonar.iac.common.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Language;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacProjectSensor;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.SonarRuntimeUtils;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.predicates.FileType;
import org.sonar.iac.common.predicates.YamlFileTypeResolver;
import org.sonar.iac.common.yaml.visitors.YamlHighlightingVisitor;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;

public abstract class AbstractYamlLanguageSensor extends IacSensor {

  public static final String JSON_LANGUAGE_KEY = "json";
  public static final String YAML_LANGUAGE_KEY = "yaml";
  public static final String FILE_SEPARATOR = "---";

  protected final Checks<IacCheck> checks;
  // Shared, per-analysis resolver. Always present: it drives fileTypes()/getInputFiles selection for sensors that opt in,
  // and the predicate-path sensors (ARM, GitHub Actions, the YAML/JSON catch-all) still receive it even though they keep
  // selecting files their own way.
  protected final YamlFileTypeResolver yamlFileTypeResolver;

  protected AbstractYamlLanguageSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, Language language, List<Class<?>> checks, IacProjectSensor projectSensor,
    YamlFileTypeResolver yamlFileTypeResolver) {
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language, projectSensor);
    this.checks = checkFactory.create(repositoryKey());
    this.checks.addAnnotatedChecks(checks);
    this.yamlFileTypeResolver = yamlFileTypeResolver;
    bindChecksTelemetry(this.checks.all());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(JSON_LANGUAGE_KEY, YAML_LANGUAGE_KEY, languageKey())
      .name("IaC " + languageName() + " Sensor");
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    if (SonarRuntimeUtils.isNotSonarLintContext(sensorContext.runtime())) {
      visitors.add(new YamlHighlightingVisitor());
      visitors.add(new YamlMetricsVisitor(fileLinesContextFactory, noSonarFilter, sensorTelemetry, repositoryKey()));
    }
    visitors.addAll(preChecksVisitors());
    visitors.add(new ChecksVisitor(checks, statistics));
    return visitors;
  }

  protected List<TreeVisitor<InputFileContext>> preChecksVisitors() {
    return List.of();
  }

  /**
   * The {@link FileType}s this sensor analyzes. When non-empty, {@link #inputFiles} selects the analysis' files of these
   * types straight from the shared {@link YamlFileTypeResolver} cache (one traversal shared by all YAML based sensors)
   * instead of re-scanning the file system with {@link #mainFilePredicate}. Sensors that select files differently (the
   * YAML/JSON catch-all, or sensors that also handle non-YAML/JSON files) leave this empty and keep the predicate path.
   */
  protected Set<FileType> fileTypes() {
    return Set.of();
  }

  @Override
  protected List<InputFile> inputFiles(SensorContext sensorContext, DurationStatistics statistics) {
    var types = fileTypes();
    if (types.isEmpty()) {
      return super.inputFiles(sensorContext, statistics);
    }
    return yamlFileTypeResolver.getInputFiles(sensorContext.fileSystem(), statistics, types.toArray(new FileType[0]));
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext, DurationStatistics statistics) {
    FileSystem fileSystem = sensorContext.fileSystem();
    return fileSystem.predicates().and(
      fileSystem.predicates().hasLanguages(JSON_LANGUAGE_KEY, YAML_LANGUAGE_KEY, languageKey()),
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      customFilePredicate(sensorContext, statistics));
  }

  /**
   * The predicate used by {@link #mainFilePredicate} on the predicate selection path. Defaults to matching nothing;
   * sensors that stay on the predicate path (e.g. ARM, which also handles non-YAML/JSON files) override it.
   */
  protected FilePredicate customFilePredicate(SensorContext sensorContext, DurationStatistics statistics) {
    return sensorContext.fileSystem().predicates().none();
  }

}
