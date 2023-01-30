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
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.LiteralList;
import org.sonar.iac.docker.tree.api.Param;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class RunInstructionImpl extends AbstractCommandInstructionImpl implements RunInstruction {

  private final List<Param> options;

  public RunInstructionImpl(SyntaxToken keyword, List<Param> options, @Nullable LiteralList arguments) {
    super(keyword, arguments);
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
    if (arguments != null) {
      result.add(arguments);
    }
    return result;
  }

  @Override
  public List<Param> options() {
    return options;
  }
}
