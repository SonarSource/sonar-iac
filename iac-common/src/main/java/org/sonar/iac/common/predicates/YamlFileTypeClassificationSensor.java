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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.iac.common.extension.DurationStatistics;

/**
 * Classifies the analysis' YAML/JSON candidate files once, in the {@link Phase.Name#PRE} phase, so the analysis sensors
 * that follow only read types from the shared {@link YamlFileTypeCache} instead of one of them shouldering the whole
 * classification.
 * <p>
 * Optimization and attribution only: the cost is reported here rather than under whichever sensor ran first.
 * Classification is memoized in {@link YamlFileTypeResolver}, so correctness does not depend on this sensor running - a
 * consumer running first still classifies lazily, exactly once.
 */
@Phase(name = Phase.Name.PRE)
public class YamlFileTypeClassificationSensor implements Sensor {

  public static final String SENSOR_NAME = "IaC YAML File Type Classification Sensor";
  private static final Logger LOG = LoggerFactory.getLogger(YamlFileTypeClassificationSensor.class);

  private final YamlFileTypeResolver yamlFileTypeResolver;

  public YamlFileTypeClassificationSensor(YamlFileTypeResolver yamlFileTypeResolver) {
    this.yamlFileTypeResolver = yamlFileTypeResolver;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    // Restrict to the candidate languages so the scan runs only for projects containing YAML/JSON (or files reassigned
    // to a specialized IaC language), not for pure non-IaC projects.
    descriptor
      .name(SENSOR_NAME)
      .onlyOnLanguages(yamlFileTypeResolver.candidateLanguages().toArray(new String[0]));
  }

  @Override
  public void execute(SensorContext sensorContext) {
    var statistics = new DurationStatistics(sensorContext.config());
    yamlFileTypeResolver.classify(sensorContext.fileSystem(), statistics);
    statistics.log();
    if (LOG.isDebugEnabled()) {
      LOG.debug("Pre-classified YAML/JSON candidate files under '{}'", sensorContext.fileSystem().baseDir());
    }
  }
}
