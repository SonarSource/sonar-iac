/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.HealthCheckInstruction;
import org.sonar.iac.docker.tree.api.Instruction;
import org.sonar.iac.docker.tree.api.NoneInstruction;
import org.sonar.iac.docker.tree.api.Param;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class HealthCheckInstructionImpl extends InstructionImpl implements HealthCheckInstruction {

  private final List<Param> options;
  private final Instruction instruction;

  public HealthCheckInstructionImpl(SyntaxToken keyword, List<Param> options, Instruction instruction) {
    super(keyword);
    this.options = options;
    this.instruction = instruction;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword, instruction);
  }

  @Override
  public boolean isNone() {
    return instruction instanceof NoneInstruction;
  }

  @Override
  public List<Param> options() {
    return options;
  }

  @Override
  public Instruction instruction() {
    return instruction;
  }

  @Override
  public Kind getKind() {
    return Kind.HEALTHCHECK;
  }
}
