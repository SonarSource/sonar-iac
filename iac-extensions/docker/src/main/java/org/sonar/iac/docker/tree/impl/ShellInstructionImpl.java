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
