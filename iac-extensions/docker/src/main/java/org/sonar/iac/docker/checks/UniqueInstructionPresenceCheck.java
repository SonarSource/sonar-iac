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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Instruction;

@Rule(key = "S6589")
public class UniqueInstructionPresenceCheck implements IacCheck {

  private static final String MESSAGE = "Remove this %s instruction which will be ignored.";
  private static final Map<DockerTree.Kind, String> UNIQUE_INSTRUCTION_KINDS_WITH_LABEL = Map.of(
    DockerTree.Kind.CMD, "CMD",
    DockerTree.Kind.ENTRYPOINT, "ENTRYPOINT");

  @Override
  public void initialize(InitContext init) {
    init.register(Body.class, UniqueInstructionPresenceCheck::checkUniqueInstruction);
  }

  private static void checkUniqueInstruction(CheckContext ctx, Body body) {
    for (DockerImage image : body.dockerImages()) {
      var foundInstructionsPerKind = searchInstructionOfKinds(image.instructions(), UNIQUE_INSTRUCTION_KINDS_WITH_LABEL.keySet());
      reportIssueOnDuplicateInstruction(ctx, foundInstructionsPerKind);
    }
  }

  private static Map<DockerTree.Kind, List<Instruction>> searchInstructionOfKinds(List<Instruction> instructions, Set<DockerTree.Kind> kinds) {
    Map<DockerTree.Kind, List<Instruction>> foundInstructionsOfKind = new EnumMap<>(DockerTree.Kind.class);
    instructions.stream()
      .filter(instruction -> kinds.contains(instruction.getKind()))
      .forEach(instruction -> foundInstructionsOfKind.computeIfAbsent(instruction.getKind(), key -> new ArrayList<>()).add(instruction));
    return foundInstructionsOfKind;
  }

  private static void reportIssueOnDuplicateInstruction(CheckContext ctx, Map<DockerTree.Kind, List<Instruction>> instructionsPerKind) {
    for (var instructionsWithKind : instructionsPerKind.entrySet()) {
      String kindLabel = UNIQUE_INSTRUCTION_KINDS_WITH_LABEL.get(instructionsWithKind.getKey());
      List<Instruction> instructions = instructionsWithKind.getValue();
      for (Instruction instruction : instructions.subList(0, instructions.size() - 1)) {
        ctx.reportIssue(instruction, String.format(MESSAGE, kindLabel));
      }
    }
  }

}
