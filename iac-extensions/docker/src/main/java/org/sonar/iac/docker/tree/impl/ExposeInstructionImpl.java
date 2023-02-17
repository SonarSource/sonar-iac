/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ExposeInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ExposeInstructionImpl extends InstructionImpl implements ExposeInstruction {

  private final List<Argument> arguments;

  public ExposeInstructionImpl(SyntaxToken keyword, List<Argument> arguments) {
    super(keyword);
    this.arguments = arguments;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(keyword);
    children.addAll(arguments);
    return children;
  }
  @Override
  public Kind getKind() {
    return Kind.EXPOSE;
  }

  @Override
  public List<Argument> arguments() {
    return arguments;
  }
}
