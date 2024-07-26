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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.LabelInstruction;
import org.sonar.iac.docker.tree.api.OnBuildInstruction;

@Rule(key = "S7028")
public class MandatoryLabelCheck implements IacCheck {
  private static final String MESSAGE = "Add the missing label%s: %s.";
  private static final String DEFAULT_REQUIRED_LABELS = "maintainer";
  private Set<String> requiredLabelSet = Set.of(DEFAULT_REQUIRED_LABELS);

  @RuleProperty(
    key = "labels",
    description = "Comma separated list of required labels.",
    defaultValue = DEFAULT_REQUIRED_LABELS)
  String requiredLabels = DEFAULT_REQUIRED_LABELS;

  @Override
  public void initialize(InitContext init) {
    init.register(Body.class, this::checkBody);
    requiredLabelSet = splitLabels(requiredLabels);
  }

  static Set<String> splitLabels(String labels) {
    return Arrays.stream(labels.split(","))
      .map(String::trim)
      .filter(s -> !s.isBlank())
      .collect(Collectors.toSet());
  }

  private void checkBody(CheckContext ctx, Body body) {
    if (body.dockerImages().isEmpty()) {
      return;
    }

    Set<String> encounteredLabels = new HashSet<>();

    body.dockerImages().stream()
      .flatMap(MandatoryLabelCheck::gatherLabelInstructions)
      .forEach(labelInstruction -> handleLabelInstruction(labelInstruction, encounteredLabels));

    List<String> missingLabels = new ArrayList<>(requiredLabelSet);
    missingLabels.removeAll(encounteredLabels);
    if (!missingLabels.isEmpty()) {
      if (missingLabels.size() == 1) {
        ctx.reportIssue(body.dockerImages().get(0).from(), MESSAGE.formatted("", "\"" + missingLabels.get(0) + "\""));
      } else {
        ctx.reportIssue(body.dockerImages().get(0).from(), MESSAGE.formatted("s", formatLabels(missingLabels)));
      }
    }
  }

  private static Stream<LabelInstruction> gatherLabelInstructions(DockerImage image) {
    return image.instructions().stream()
      .filter(instruction -> instruction instanceof LabelInstruction
        || (instruction instanceof OnBuildInstruction onBuildInstruction
          && onBuildInstruction.instruction() instanceof LabelInstruction))
      .map(instruction -> {
        if (instruction instanceof LabelInstruction labelInstruction) {
          return labelInstruction;
        } else {
          return ((LabelInstruction) ((OnBuildInstruction) instruction).instruction());
        }
      });
  }

  private static void handleLabelInstruction(LabelInstruction labelInstruction, Set<String> encounteredLabels) {
    labelInstruction.labels().stream()
      .map(label -> ArgumentResolution.of(label.key()).value())
      .map(String::toLowerCase)
      .forEach(encounteredLabels::add);
  }

  static String formatLabels(List<String> missingLabels) {
    var sb = new StringBuilder();
    Collections.sort(missingLabels);
    for (var i = 0; i < missingLabels.size(); i++) {
      sb.append("\"");
      sb.append(missingLabels.get(i));
      sb.append("\"");
      if (i == missingLabels.size() - 2) {
        sb.append(" and ");
      } else if (i < missingLabels.size() - 2) {
        sb.append(", ");
      }
    }

    return sb.toString();
  }
}
