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
package org.sonar.iac.docker.tree.impl;

import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.StopSignalInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class StopSignalInstructionImpl extends InstructionImpl implements StopSignalInstruction {

  private final Argument signal;

  public StopSignalInstructionImpl(SyntaxToken keyword, Argument signal) {
    super(keyword);
    this.signal = signal;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword(), signal);
  }

  @Override
  public Kind getKind() {
    return Kind.STOPSIGNAL;
  }

  @Override
  public Argument signal() {
    return signal;
  }
}
