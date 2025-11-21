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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class RunInstructionImpl extends AbstractShellCodeInstructionImpl implements RunInstruction {

  private final List<Flag> options;

  public RunInstructionImpl(SyntaxToken keyword, List<Flag> options, @Nullable Tree code) {
    super(keyword, code);
    this.options = options;
  }

  @Override
  public Kind getKind() {
    return Kind.RUN;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(keyword);
    result.addAll(options);
    if (code != null) {
      result.add(code);
    }
    return result;
  }

  @Override
  public List<Flag> options() {
    return options;
  }

  @Override
  public String toString() {
    return keyword + " " + code;
  }
}
