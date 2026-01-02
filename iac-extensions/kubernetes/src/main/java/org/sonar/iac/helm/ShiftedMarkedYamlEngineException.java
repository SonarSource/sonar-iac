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
package org.sonar.iac.helm;

import java.util.Optional;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;

public class ShiftedMarkedYamlEngineException extends MarkedYamlEngineException {
  private final Mark originalProblemMark;
  private final Mark shiftedProblemMark;

  public ShiftedMarkedYamlEngineException(MarkedYamlEngineException original, Mark shiftedMark) {
    super(original.getContext(), original.getContextMark(), original.getProblem(), Optional.of(shiftedMark), original.getCause());
    this.originalProblemMark = original.getProblemMark().orElse(shiftedMark);
    this.shiftedProblemMark = shiftedMark;
  }

  public String describeShifting() {
    // snakeyaml-engine is 0-based, so +1; shifted location comes from LocationShifter and is already adjusted
    return "from [" + (originalProblemMark.getLine() + 1) + ":" + (originalProblemMark.getColumn() + 1) +
      "] to [" + (shiftedProblemMark.getLine() + 1) + ":" + (shiftedProblemMark.getColumn() + 1) + "]";
  }
}
