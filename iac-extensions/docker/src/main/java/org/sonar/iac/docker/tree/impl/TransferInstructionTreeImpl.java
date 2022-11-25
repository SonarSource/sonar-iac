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
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.TransferInstructionTree;
import org.sonar.iac.docker.tree.api.LiteralListTree;
import org.sonar.iac.docker.tree.api.ParamTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

/**
 * To be used when we want to implement a command that expect one+ src with one dest (supporting both SHELL and EXEC format) with Params.
 * Examples :
 * {@code MY_CMD --param=value src1 src2 dest}
 * {@code MY_CMD --param=value ["src1", "src2", "dest"]}
 */
public abstract class TransferInstructionTreeImpl extends InstructionTreeImpl implements TransferInstructionTree {
  protected final List<ParamTree> options;
  protected final LiteralListTree srcsAndDest;

  protected TransferInstructionTreeImpl(SyntaxToken add, List<ParamTree> options, LiteralListTree srcsAndDest) {
    super(add);
    this.options = options;
    this.srcsAndDest = srcsAndDest;
  }

  @Override
  public List<ParamTree> options() {
    return options;
  }

  @Override
  public List<SyntaxToken> srcs() {
    List<SyntaxToken> srcs = srcsAndDest.literals();
    return srcs.subList(0, srcs.size()-1);
  }

  @Override
  public SyntaxToken dest() {
    List<SyntaxToken> dest = srcsAndDest.literals();
    return dest.get(dest.size()-1);
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(keyword);
    children.addAll(options);
    children.add(srcsAndDest);
    return children;
  }
}
