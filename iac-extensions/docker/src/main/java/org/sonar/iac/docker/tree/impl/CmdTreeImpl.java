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
import org.sonar.iac.docker.tree.api.CmdTree;
import org.sonar.iac.docker.tree.api.ExecFormLiteralTree;
import org.sonar.iac.docker.tree.api.ExecFormTree;
import org.sonar.iac.docker.tree.api.SeparatedList;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class CmdTreeImpl extends InstructionTreeImpl implements CmdTree {

  private final ExecFormTree execForm;

  public CmdTreeImpl(SyntaxToken keyword, @Nullable ExecFormTree execForm) {
    super(keyword);
    this.execForm = execForm;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(keyword);
    if (execForm != null) {
      result.add(execForm);
    }
    return result;
  }

  @Override
  public Kind getKind() {
    return Kind.CMD;
  }

  @Override
  public ExecFormTree execForm() {
    return execForm;
  }

  @Override
  public List<SyntaxToken> cmdArguments() {
    List<SyntaxToken> result = new ArrayList<>();
    SeparatedList<ExecFormLiteralTree> literals = execForm.literals();
    if (literals != null) {
      for (ExecFormLiteralTree element : literals.elements()) {
        result.add(element.value());
      }
    }
    return result;
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }
}
