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
import org.sonar.iac.docker.tree.api.EntrypointTree;
import org.sonar.iac.docker.tree.api.LiteralListTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class EntrypointTreeImpl extends InstructionTreeImpl implements EntrypointTree {

  private final LiteralListTree argumentList;

  public EntrypointTreeImpl(SyntaxToken keyword, @Nullable LiteralListTree argumentList) {
    super(keyword);
    this.argumentList = argumentList;
  }


  @Override
  @CheckForNull
  public LiteralListTree entrypointArguments() {
    return argumentList;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(keyword);
    if (argumentList != null) {
      result.add(argumentList);
    }
    return result;
  }

  @Override
  public Kind getKind() {
    return Kind.ENTRYPOINT;
  }
}
