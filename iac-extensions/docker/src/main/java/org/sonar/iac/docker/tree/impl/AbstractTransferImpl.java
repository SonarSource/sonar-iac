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
import java.util.stream.Collectors;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ArgumentsForm;
import org.sonar.iac.docker.tree.api.TransferInstruction;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.utils.ArgumentUtils;

/**
 * To be used when we want to implement a command that expect one+ src with one dest (supporting both SHELL and EXEC format) with Params.
 * Examples :
 * {@code MY_CMD --param=value src1 src2 dest}
 * {@code MY_CMD --param=value ["src1", "src2", "dest"]}
 */
public abstract class AbstractTransferImpl extends InstructionImpl implements TransferInstruction {
  protected final List<Flag> options;
  protected final ArgumentsForm srcsAndDest;

  protected AbstractTransferImpl(SyntaxToken add, List<Flag> options, ArgumentsForm srcsAndDest) {
    super(add);
    this.options = options;
    this.srcsAndDest = srcsAndDest;
  }

  @Override
  public List<Flag> options() {
    return options;
  }

  @Override
  public List<SyntaxToken> srcs() {
    List<Argument> args = srcsAndDest.arguments();
    List<Argument> srcs = args.subList(0, args.size()-1);
    return srcs.stream()
      .map(ArgumentUtils::resolve)
      .map(ArgumentUtils.ArgumentResolution::asSyntaxToken)
      .collect(Collectors.toList());
  }

  @Override
  public SyntaxToken dest() {
    List<Argument> args = srcsAndDest.arguments();
    Argument dest = args.get(args.size()-1);
    return ArgumentUtils.resolve(dest).asSyntaxToken();
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
