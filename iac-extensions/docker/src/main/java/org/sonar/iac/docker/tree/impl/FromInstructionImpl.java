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
import org.sonar.iac.docker.tree.api.Alias;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class FromInstructionImpl extends InstructionImpl implements FromInstruction {

  private final Flag platform;
  private final Argument image;
  private final Alias alias;

  public FromInstructionImpl(SyntaxToken keyword, @Nullable Flag platform, Argument image, @Nullable Alias alias) {
    super(keyword);
    this.platform = platform;
    this.image = image;
    this.alias = alias;
  }

  @Nullable
  @Override
  public Flag platform() {
    return platform;
  }

  @Override
  public Argument image() {
    return image;
  }

  @Nullable
  @Override
  public Alias alias() {
    return alias;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(keyword);
    if (platform != null) {
      children.add(platform);
    }
    children.add(image);
    if (alias != null) {
      children.add(alias);
    }
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.FROM;
  }
}
