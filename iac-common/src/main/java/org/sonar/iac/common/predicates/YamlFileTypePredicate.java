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
 * A {@link FilePredicate} that, when it matches a file, identifies it as being of a given {@link FileType}.
 * {@link YamlFileTypeResolver} applies such predicates in order and maps the first match to its {@link #fileType()},
 * which removes the need to pair each predicate with its file type externally.
 */
public interface YamlFileTypePredicate extends FilePredicate {

  FileType fileType();

  /**
   * Binds this predicate to the timers of the given {@link DurationStatistics} for the current sensor execution. It must
   * be called before {@link #apply(InputFile)}, so that the same predicate instance can be reused across executions.
   */
  void applyTimers(DurationStatistics durationStatistics);
}
