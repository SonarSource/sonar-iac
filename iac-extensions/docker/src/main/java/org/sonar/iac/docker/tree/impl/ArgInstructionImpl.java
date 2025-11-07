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
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ArgInstructionImpl extends InstructionImpl implements ArgInstruction {
  private final List<KeyValuePair> keyValuePairs;

  public ArgInstructionImpl(SyntaxToken arg, List<KeyValuePair> argNames) {
    super(arg);
    this.keyValuePairs = argNames;
  }

  @Override
  public List<KeyValuePair> keyValuePairs() {
    return keyValuePairs;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(keyword);
    children.addAll(keyValuePairs);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.ARG;
  }
}
