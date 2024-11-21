/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.predicates;

import java.nio.file.Path;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.filesystem.FileSystemUtils;

public class HelmProjectMemberPredicate implements FilePredicate {
  private final SensorContext sensorContext;

  public HelmProjectMemberPredicate(SensorContext sensorContext) {
    this.sensorContext = sensorContext;
  }

  @Override
  public boolean apply(InputFile inputFile) {
    return FileSystemUtils.retrieveHelmProjectFolder(Path.of(inputFile.uri()), sensorContext.fileSystem()) != null;
  }

}
