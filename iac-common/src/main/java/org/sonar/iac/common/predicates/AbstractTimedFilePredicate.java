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

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.DurationStatistics;

/**
 * Base class for file predicates whose execution time should be recorded.
 * <p>
 * The timer is not provided at construction time: a predicate can be instantiated once (for example by
 * {@link YamlFileTypeResolver}) and reused across several sensor executions. Each predicate knows the name of its own
 * timer ({@link #timerName()}) and binds it to the current execution through {@link #applyTimers(DurationStatistics)},
 * which must be called before the predicate is applied.
 */
public abstract class AbstractTimedFilePredicate implements FilePredicate {

  private DurationStatistics.Timer timer;

  protected AbstractTimedFilePredicate() {
    // The timer is bound later through applyTimers, so that the same predicate instance can be reused across sensor executions.
  }

  /**
   * Binds this predicate to a timer of the given {@link DurationStatistics}, using {@link #timerName()} as the timer id.
   */
  public final void applyTimers(DurationStatistics durationStatistics) {
    this.timer = durationStatistics.timer(timerName());
  }

  /**
   * Name of the timer used to record this predicate's execution time. Defaults to the simple class name; predicates that
   * need a more specific name (for example one per file kind) override this method.
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
