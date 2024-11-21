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
    if (cmdInstruction != null) {
      result.add(cmdInstruction);
    }
    if (none != null) {
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
