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
package org.sonar.iac.common.extension;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;

public abstract class AbstractTimedFilePredicate implements FilePredicate {

  private final DurationStatistics.Timer timer;

  protected AbstractTimedFilePredicate(DurationStatistics.Timer timer) {
    this.timer = timer;
  }

  @Override
  public boolean apply(InputFile inputFile) {
    return timer.time(() -> accept(inputFile));
  }

  protected abstract boolean accept(InputFile inputFile);
}
