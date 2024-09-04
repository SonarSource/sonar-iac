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
package org.sonar.iac.common.extension;

import java.util.List;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Language;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.extension.analyzer.Analyzer;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

public abstract class IacSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(IacSensor.class);

  public static final String FAIL_FAST_PROPERTY_NAME = "sonar.internal.analysis.failFast";

  protected final SonarRuntime sonarRuntime;
  protected final FileLinesContextFactory fileLinesContextFactory;
  protected final NoSonarFilter noSonarFilter;
  protected final Language language;

  protected IacSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter,
    Language language) {
    this.sonarRuntime = sonarRuntime;
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    this.language = language;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage(languageKey())
      .name("IaC " + languageName() + " Sensor");

    if (sonarRuntime.getApiVersion().isGreaterThanOrEqual(Version.create(9, 3))) {
      descriptor.processesFilesIndependently();
    }
  }

  protected String languageName() {
    return language.getName();
  }

  protected String languageKey() {
    return language.getKey();
  }

  protected abstract Analyzer createAnalyzer(SensorContext sensorContext, DurationStatistics statistics);

  protected abstract String repositoryKey();

  protected abstract List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics);

  protected abstract String getActivationSettingKey();

  @Override
  public void execute(SensorContext sensorContext) {
    // TODO to remove, it is for local tests in SonarLint
    LOG.info("AAAAA Starting sensor " + getClass().getName());
    if (!isActive(sensorContext)) {
      return;
    }

    if (isNotSonarLintContext(sensorContext)) {
      importExternalReports(sensorContext);
    }

    initContext(sensorContext);

    var statistics = new DurationStatistics(sensorContext.config());
    List<InputFile> inputFiles = inputFiles(sensorContext);
    var analyzer = createAnalyzer(sensorContext, statistics);
    analyzer.analyseFiles(sensorContext, inputFiles, languageName());
    statistics.log();
    afterExecute();
  }

  protected void initContext(SensorContext sensorContext) {
    // do nothing by default
  }

  protected List<InputFile> inputFiles(SensorContext sensorContext) {
    var fileSystem = sensorContext.fileSystem();
    var predicate = mainFilePredicate(sensorContext);
    return StreamSupport.stream(fileSystem.inputFiles(predicate).spliterator(), false)
      .toList();
  }

  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    var fileSystem = sensorContext.fileSystem();
    return fileSystem.predicates().and(
      fileSystem.predicates().hasLanguage(languageKey()),
      fileSystem.predicates().hasType(InputFile.Type.MAIN));
  }

  protected void importExternalReports(SensorContext sensorContext) {
    // Default is to do nothing. A child-sensor that does require importing external reports should override this.
  }

  public static boolean isNotSonarLintContext(SensorContext sensorContext) {
    return sensorContext.runtime().getProduct() != SonarProduct.SONARLINT;
  }

  private boolean isActive(SensorContext sensorContext) {
    return sensorContext.config().getBoolean(getActivationSettingKey()).orElse(false);
  }

  protected void afterExecute() {
    // do nothing by default
  }

  public static boolean isFailFast(SensorContext context) {
    return context.config().getBoolean(FAIL_FAST_PROPERTY_NAME).orElse(false);
  }

}
