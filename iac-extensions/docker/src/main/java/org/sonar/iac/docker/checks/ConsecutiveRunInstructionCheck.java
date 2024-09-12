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
package org.sonar.iac.docker.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.Instruction;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S7031")
public class ConsecutiveRunInstructionCheck implements IacCheck {
  private static final String PRIMARY_MESSAGE = "Merge this RUN instruction with the consecutive ones.";
  private static final String SECONDARY_MESSAGE = "consecutive RUN instruction";

  @Override
  public void initialize(InitContext init) {
    init.register(DockerImage.class, ConsecutiveRunInstructionCheck::checkFromRunsInstruction);
  }

  private static void checkFromRunsInstruction(CheckContext ctx, DockerImage image) {
    var listOfConsecutiveRunInstructions = extractConsecutiveRunInstructions(image);
    for (List<RunInstruction> consecutiveRunInstructions : listOfConsecutiveRunInstructions) {
      var secondaryLocations = consecutiveRunInstructions.stream()
        .skip(1)
        .map(runInstruction -> new SecondaryLocation(runInstruction.keyword(), SECONDARY_MESSAGE))
        .toList();
      ctx.reportIssue(consecutiveRunInstructions.get(0).keyword(), PRIMARY_MESSAGE, secondaryLocations);
    }
  }

  private static List<List<RunInstruction>> extractConsecutiveRunInstructions(DockerImage image) {
    var result = new ArrayList<List<RunInstruction>>();
    var instructions = image.instructions();
    var i = 0;
    while (i < instructions.size()) {
      if (instructions.get(i).is(DockerTree.Kind.RUN)) {
        i = nextIteration(i, instructions, result);
      } else {
        i++;
      }
    }
    return result;
  }

  private static int nextIteration(int i, List<Instruction> instructions, List<List<RunInstruction>> result) {
    var subList = new ArrayList<RunInstruction>();
    subList.add((RunInstruction) instructions.get(i));
    i++;
    while (i < instructions.size() && instructions.get(i) instanceof RunInstruction current) {
      var previous = subList.get(subList.size() - 1);
      if (haveEqualOptions(current, previous)) {
        subList.add(current);
        i++;
      } else {
        // start next iteration from the current instruction; on the next iteration of the outer loop,
        // the next instruction will be compared with the current one
        i--;
        break;
      }
    }
    if (subList.size() > 1) {
      result.add(subList);
    }
    return i + 1;
  }

  private static boolean haveEqualOptions(RunInstruction first, RunInstruction second) {
    if (first.options().size() != second.options().size()) {
      return false;
    }
    if (first.options().isEmpty()) {
      return true;
    }

    var firstOptions = first.options().stream().map(Flag::toString).collect(Collectors.toSet());
    var secondOptions = second.options().stream().map(Flag::toString).collect(Collectors.toSet());
    return firstOptions.equals(secondOptions);
  }
}
