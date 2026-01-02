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
package org.sonar.iac.common.reports;

import java.io.File;
import java.util.Collection;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

public abstract class AbstractExternalReportSensor<T extends AbstractJsonReportImporter> implements Sensor {
  @Override
  public void describe(SensorDescriptor sensorDescriptor) {
    sensorDescriptor
      .onlyOnLanguages(getLanguageKeys())
      .name("IaC " + getName() + " Sensor");
  }

  @Override
  public void execute(SensorContext sensorContext) {
    var importer = createImporter(sensorContext);
    getReportFiles(sensorContext).forEach(importer::importReport);
  }

  protected abstract String[] getLanguageKeys();

  protected abstract String getName();

  protected abstract T createImporter(SensorContext sensorContext);

  protected abstract Collection<File> getReportFiles(SensorContext sensorContext);
}
