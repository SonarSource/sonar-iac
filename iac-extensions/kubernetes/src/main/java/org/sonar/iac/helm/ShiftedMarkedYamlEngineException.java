/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
