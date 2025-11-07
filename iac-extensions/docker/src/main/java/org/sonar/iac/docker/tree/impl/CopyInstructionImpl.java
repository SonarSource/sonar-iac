/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ArgumentList;
import org.sonar.iac.docker.tree.api.CopyInstruction;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class CopyInstructionImpl extends AbstractTransferInstructionImpl implements CopyInstruction {

  public CopyInstructionImpl(SyntaxToken add, List<Flag> options, ArgumentList srcsAndDest) {
    super(add, options, srcsAndDest);
  }

  @Override
  public Kind getKind() {
    return Kind.COPY;
  }

  @Override
  public List<Argument> arguments() {
    return srcsAndDest.arguments();
  }

  @CheckForNull
  @Override
  public Kind getKindOfArgumentList() {
    return srcsAndDest.getKind();
  }
}
