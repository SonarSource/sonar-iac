/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ArgumentList;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.TransferInstruction;

/**
 * To be used when we want to implement a command that expect one+ src with one dest (supporting both SHELL and EXEC format) with Params.
 * Examples :
 * {@code MY_CMD --param=value src1 src2 dest}
 * {@code MY_CMD --param=value ["src1", "src2", "dest"]}
 */
public abstract class AbstractTransferInstructionImpl extends InstructionImpl implements TransferInstruction {
  protected final List<Flag> options;
  protected final ArgumentList srcsAndDest;

  protected AbstractTransferInstructionImpl(SyntaxToken add, List<Flag> options, ArgumentList srcsAndDest) {
    super(add);
    this.options = options;
    this.srcsAndDest = srcsAndDest;
  }

  @Override
  public List<Flag> options() {
    return options;
  }

  @Override
  public List<Argument> srcs() {
    List<Argument> args = srcsAndDest.arguments();
    return args.subList(0, args.size() - 1);
  }

  @Override
  public Argument dest() {
    List<Argument> args = srcsAndDest.arguments();
    return args.get(args.size() - 1);
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
