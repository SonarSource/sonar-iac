/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.common.json;

import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFileFilter;

public class JsonFileFilter implements InputFileFilter {

  private static final Set<String> FILTERED_FILENAME_FRAGMENTS = Set.of("build-wrapper-dump", "compile_commands");

  @Override
  public boolean accept(InputFile inputFile) {
    if (!JsonLanguage.KEY.equals(inputFile.language())) {
      return true;
    }

    return FILTERED_FILENAME_FRAGMENTS.stream().noneMatch(inputFile.filename()::contains);
  }
}
