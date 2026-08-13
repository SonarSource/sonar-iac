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

import org.jspecify.annotations.Nullable;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.DurationStatistics;

/**
 * Base class for file predicates whose execution time should be recorded. The timer is bound after construction via
 * {@link #applyTimers(DurationStatistics)}, so the predicate instance can be reused across sensor executions.
 */
public abstract class AbstractTimedFilePredicate implements FilePredicate {

  private DurationStatistics.@Nullable Timer timer;

  public final void applyTimers(DurationStatistics durationStatistics) {
    this.timer = durationStatistics.timer(timerName());
  }

  /**
   * Defaults to the simple class name; override for a more specific name (e.g. one per file kind).
   */
  protected String timerName() {
    return getClass().getSimpleName();
  }

  @Override
  public final boolean apply(InputFile inputFile) {
    if (timer == null) {
      throw new IllegalStateException("Timers must be applied before applying the predicate");
    }
    return timer.time(() -> accept(inputFile));
  }

  /**
   * Decides whether the given file matches this predicate. Subclasses implement the actual matching logic here; it is
   * invoked by {@link #apply(InputFile)} within the bound timer, so implementations must not record timing themselves.
   */
  protected abstract boolean accept(InputFile inputFile);
}
