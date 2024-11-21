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
package org.sonar.iac.docker.checks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    RunInstruction currentInstruction = (RunInstruction) instructions.get(i);
    subList.add(currentInstruction);
    while (i + 1 < instructions.size() && instructions.get(i + 1) instanceof RunInstruction nextInstruction) {
      if (haveEqualOptions(currentInstruction, nextInstruction)) {
        subList.add(nextInstruction);
        currentInstruction = nextInstruction;
        i++;
      } else {
        break;
      }
    }
    if (subList.size() > 1) {
      result.add(subList);
    }
    return i + 1;
  }

  private static boolean haveEqualOptions(RunInstruction first, RunInstruction second) {
    Set<Flag> firstOptions = new HashSet<>(first.options());
    Set<Flag> secondOptions = new HashSet<>(second.options());
    return firstOptions.equals(secondOptions);
  }
}
