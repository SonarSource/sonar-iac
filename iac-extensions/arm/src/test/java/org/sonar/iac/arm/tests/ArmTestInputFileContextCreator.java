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
package org.sonar.iac.arm.tests;

import java.io.File;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.arm.plugin.ArmLanguage;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.languages.IacLanguage;

public class ArmTestInputFileContextCreator {

  public static InputFileContext jsonFileContext() {
    InputFile inputFile = new TestInputFileBuilder("moduleKey", "file.json")
      .setLanguage("json")
      .build();
    return new InputFileContext(SensorContextTester.create(new File(".")), inputFile, IacLanguage.ARM);
  }

  public static InputFileContext bicepFileContext() {
    InputFile inputFile = new TestInputFileBuilder("moduleKey", "file.bicep")
      .setLanguage(ArmLanguage.KEY)
      .build();
    return new InputFileContext(SensorContextTester.create(new File(".")), inputFile, IacLanguage.ARM);
  }
}
