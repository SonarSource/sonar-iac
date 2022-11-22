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
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.CmdTree;
import org.sonar.iac.docker.tree.api.HealthCheckTree;
import org.sonar.iac.docker.tree.api.ParamTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class HealthCheckTreeImpl extends InstructionTreeImpl implements HealthCheckTree {

  private final SyntaxToken none;
  private final List<ParamTree> options;
  private final CmdTree cmd;

  public HealthCheckTreeImpl(SyntaxToken keyword, SyntaxToken none) {
    super(keyword);
    this.none = none;
    this.options = null;
    this.cmd = null;
  }

  public HealthCheckTreeImpl(SyntaxToken keyword, List<ParamTree> options, CmdTree cmd) {
    super(keyword);
    this.none = null;
    this.options = options;
    this.cmd = cmd;
  }

  @Override
  public List<Tree> children() {
    if (isNone()) {
      return List.of(keyword, none);
    } else {
      List<Tree> children = new ArrayList<>();
      children.add(keyword);
      children.addAll(options);
      children.add(cmd);
      return children;
    }
  }

  @Override
  public boolean isNone() {
    return none != null;
  }

  @CheckForNull
  @Override
  public List<ParamTree> options() {
    return options;
  }

  @CheckForNull
  @Override
  public CmdTree cmd() {
    return cmd;
  }

  @Override
  public Kind getKind() {
    return Kind.HEALTHCHECK;
  }
}
