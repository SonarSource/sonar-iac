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
package org.sonar.iac.common.yaml;

import java.util.ArrayList;
import java.util.List;
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
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.visitors.YamlHighlightingVisitor;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;

public abstract class AbstractYamlLanguageSensor extends IacSensor {

  public static final String JSON_LANGUAGE_KEY = "json";
  public static final String YAML_LANGUAGE_KEY = "yaml";
  public static final String FILE_SEPARATOR = "---";

  protected final Checks<IacCheck> checks;

  protected AbstractYamlLanguageSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, Language language, List<Class<?>> checks) {
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language);
    this.checks = checkFactory.create(repositoryKey());
    this.checks.addAnnotatedChecks(checks);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(JSON_LANGUAGE_KEY, YAML_LANGUAGE_KEY, language.getKey())
      .name("IaC " + language.getName() + " Sensor");
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    if (isNotSonarLintContext(sensorContext.runtime())) {
      visitors.add(new YamlHighlightingVisitor());
      visitors.add(new YamlMetricsVisitor(fileLinesContextFactory, noSonarFilter, sensorTelemetry));
    }
    visitors.add(new ChecksVisitor(checks, statistics));
    return visitors;
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext, DurationStatistics statistics) {
    FileSystem fileSystem = sensorContext.fileSystem();
    return fileSystem.predicates().and(
      fileSystem.predicates().hasLanguages(JSON_LANGUAGE_KEY, YAML_LANGUAGE_KEY, language.getKey()),
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      customFilePredicate(sensorContext, statistics));
  }

  protected abstract FilePredicate customFilePredicate(SensorContext sensorContext, DurationStatistics statistics);

}
