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
package org.sonar.iac.docker.tree.impl;

import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.ExecForm;
import org.sonar.iac.docker.tree.api.ShellInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ShellInstructionImpl extends InstructionImpl implements ShellInstruction {

  private final ExecForm arguments;

  public ShellInstructionImpl(SyntaxToken keyword, ExecForm shellForm) {
    super(keyword);
    this.arguments = shellForm;
  }

  @Override
  @CheckForNull
  public ExecForm arguments() {
    return arguments;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword, arguments);
  }

  @Override
  public Kind getKind() {
    return Kind.SHELL;
  }
}
