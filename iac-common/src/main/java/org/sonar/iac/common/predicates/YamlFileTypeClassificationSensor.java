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
package org.sonar.iac.common.predicates;

import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.SonarRuntimeUtils;

import static org.sonar.iac.common.extension.SonarRuntimeUtils.activateHiddenFilesProcessing;

/**
 * Classifies the analysis' YAML/JSON candidate files once, in the {@link Phase.Name#PRE} phase, so the analysis sensors
 * that follow only read types from the shared {@link YamlFileTypeCache} instead of one of them shouldering the whole
 * classification.
 * <p>
 * Cache-path consumers no longer call {@link YamlFileTypeResolver#classifyInputFiles} themselves, so their {@code getInputFiles} reads depend on this sensor
 * having run first.
 * Processes hidden files (see {@link SonarRuntimeUtils#activateHiddenFilesProcessing}) so that GitHub Actions files under
 * the hidden {@code .github/workflows} directory are classified here rather than lazily by the GitHub Actions sensor.
 */
@Phase(name = Phase.Name.PRE)
public class YamlFileTypeClassificationSensor implements Sensor {

  public static final String SENSOR_NAME = "IaC YAML File Type Classification Sensor";

  private final SonarRuntime sonarRuntime;
  private final YamlFileTypeResolver yamlFileTypeResolver;

  public YamlFileTypeClassificationSensor(SonarRuntime sonarRuntime, YamlFileTypeResolver yamlFileTypeResolver) {
    this.sonarRuntime = sonarRuntime;
    this.yamlFileTypeResolver = yamlFileTypeResolver;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    // TODO SCANENGINE-160
    // onlyOnLanguages optimization is not applied because it doesn't support hidden files correctly
    descriptor.name(SENSOR_NAME);
    activateHiddenFilesProcessing(sonarRuntime, descriptor);
  }

  @Override
  public void execute(SensorContext sensorContext) {
    var statistics = new DurationStatistics(sensorContext.config());
    yamlFileTypeResolver.classifyInputFiles(sensorContext, statistics);
    statistics.log();
  }
}
