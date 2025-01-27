/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.checks.utils.MultiStageBuildInspector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.CommandInstruction;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.ShellInstruction;
import org.sonar.iac.docker.tree.api.Variable;

import static org.sonar.iac.docker.tree.TreeUtils.getDockerImageName;
import static org.sonar.iac.docker.tree.TreeUtils.getParentDockerImageName;

@Rule(key = "S7019")
public class ShellFormOverExecFormCheck implements IacCheck {

  private static final String DEFAULT_MESSAGE = "Replace this shell form with exec form.";
  private static final String WRAPPING_SCRIPT_MESSAGE = "Consider wrapping this instruction in a script file and call it with exec form.";
  private static final Set<Class<? extends CommandInstruction>> classes = Set.of(
    CmdInstruction.class,
    EntrypointInstruction.class);
  private static final Pattern UNSUPPORTED_FEATURES_IN_EXEC_FORM = Pattern.compile("&&|\\|\\||\\||;");

  private final ShellInstructionsInfo checkContext = new ShellInstructionsInfo();
  private MultiStageBuildInspector multiStageBuildInspector = null;

  @Override
  public void initialize(InitContext init) {
    init.register(Body.class, this::initFileAnalysis);
    init.register(DockerImage.class, this::resetShellInstruction);
    init.register(ShellInstruction.class, this::tagDockerImageWithShellInstruction);
    classes.forEach(klass -> init.register(klass, this::checkCommandInstructionForm));
  }

  private void initFileAnalysis(CheckContext ctx, Body body) {
    multiStageBuildInspector = MultiStageBuildInspector.of(body);
    checkContext.reset();
  }

  private void resetShellInstruction(CheckContext ctx, DockerImage dockerImage) {
    checkContext.hasShellInstructionInCurrentImage = false;
  }

  private void tagDockerImageWithShellInstruction(CheckContext ctx, ShellInstruction shellInstruction) {
    checkContext.hasShellInstructionInCurrentImage = true;
    getDockerImageName(shellInstruction).ifPresent(checkContext.imageNameWithShellInstruction::add);
  }

  private void checkCommandInstructionForm(CheckContext ctx, CommandInstruction commandInstruction) {
    if (commandInstruction.getKindOfArgumentList() == DockerTree.Kind.SHELL_FORM && !checkContext.hasShellInstructionInCurrentImage
      && !hasAnyParentDockerImageWithShellInstruction(commandInstruction)) {
      var firstArg = commandInstruction.arguments().get(0);
      var lastArg = commandInstruction.arguments().get(commandInstruction.arguments().size() - 1);
      var textRange = TextRanges.mergeElementsWithTextRange(List.of(firstArg, lastArg));
      var message = DEFAULT_MESSAGE;
      if (containFeatureNotSupportedByExecForm(commandInstruction)) {
        message = WRAPPING_SCRIPT_MESSAGE;
      }
      ctx.reportIssue(textRange, message);
    }
  }

  private boolean hasAnyParentDockerImageWithShellInstruction(CommandInstruction commandInstruction) {
    return getParentDockerImageName(commandInstruction)
      .map((String parentDockerImageName) -> {
        if (hasShellInstruction(parentDockerImageName)) {
          return true;
        }
        var parentsImageName = multiStageBuildInspector.getStageDependencies(parentDockerImageName);
        return parentsImageName.stream().anyMatch(this::hasShellInstruction);
      })
      .orElse(false);
  }

  private boolean hasShellInstruction(String imageName) {
    return checkContext.imageNameWithShellInstruction.contains(imageName);
  }

  private static boolean containFeatureNotSupportedByExecForm(CommandInstruction commandInstruction) {
    for (Argument arg : commandInstruction.arguments()) {
      if (hasVariableReference(arg)) {
        return true;
      }
      var resolvedArgument = ArgumentResolution.of(arg);
      if (resolvedArgument.isResolved() && UNSUPPORTED_FEATURES_IN_EXEC_FORM.matcher(resolvedArgument.value()).find()) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasVariableReference(Tree tree) {
    return tree instanceof Variable || tree.children().stream().anyMatch(ShellFormOverExecFormCheck::hasVariableReference);
  }

  static class ShellInstructionsInfo {
    private boolean hasShellInstructionInCurrentImage = false;
    private final Set<String> imageNameWithShellInstruction = new HashSet<>();

    void reset() {
      hasShellInstructionInCurrentImage = false;
      imageNameWithShellInstruction.clear();
    }
  }
}
