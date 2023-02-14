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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.HealthCheckInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class HealthCheckInstructionImpl extends InstructionImpl implements HealthCheckInstruction {

  private final List<Flag> options;
  @Nullable
  private final CmdInstruction cmdInstruction;
  @Nullable
  private final SyntaxToken none;

  public HealthCheckInstructionImpl(SyntaxToken keyword, List<Flag> options, @Nullable CmdInstruction cmdInstruction, @Nullable SyntaxToken none) {
    super(keyword);
    this.options = options;
    this.cmdInstruction = cmdInstruction;
    this.none = none;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(keyword);
    result.addAll(options);
    if(cmdInstruction != null) {
      result.add(cmdInstruction);
    }
    if(none != null) {
      result.add(none);
    }
    return result;
  }

  @Override
  public boolean isNone() {
    return none != null;
  }

  @Override
  public List<Flag> options() {
    return options;
  }

  @Override
  @CheckForNull
  public CmdInstruction cmdInstruction() {
    return cmdInstruction;
  }

  @Override
  @CheckForNull
  public SyntaxToken none() {
    return none;
  }

  @Override
  public Kind getKind() {
    return Kind.HEALTHCHECK;
  }
}
