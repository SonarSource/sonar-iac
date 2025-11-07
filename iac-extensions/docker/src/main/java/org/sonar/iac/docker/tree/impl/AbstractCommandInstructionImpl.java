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
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ArgumentList;
import org.sonar.iac.docker.tree.api.CommandInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public abstract class AbstractCommandInstructionImpl extends InstructionImpl implements CommandInstruction {

  protected final ArgumentList arguments;

  protected AbstractCommandInstructionImpl(SyntaxToken keyword, @Nullable ArgumentList arguments) {
    super(keyword);
    this.arguments = arguments;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(keyword);
    if (arguments != null) {
      result.add(arguments);
    }
    return result;
  }

  @Override
  public List<Argument> arguments() {
    if (arguments == null) {
      return Collections.emptyList();
    }
    return arguments.arguments();
  }

  @CheckForNull
  @Override
  public Kind getKindOfArgumentList() {
    if (arguments == null) {
      return null;
    }
    return arguments.getKind();
  }
}
