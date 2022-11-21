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
import org.sonar.iac.docker.tree.api.ExecFormLiteralTree;
import org.sonar.iac.docker.tree.api.ExecFormTree;
import org.sonar.iac.docker.tree.api.InstructionWithExecOrShellFormTreeTree;
import org.sonar.iac.docker.tree.api.SeparatedList;
import org.sonar.iac.docker.tree.api.ShellFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

/**
 * This is abstract Tree implementation for instruction that contains exclusively ExecForm or ShellForm arguments.
 * Examples of such instructions: CMD, ENTRYPOINT, RUN, COPY, etc.
 * <p>
 * {@code CMD ["command","param1","param2"]} - Exec Form
 * {@code CMD command param1 param2} - Shell Form
 */
public abstract class InstructionWithExecOrShellFormTreeImpl extends InstructionTreeImpl implements InstructionWithExecOrShellFormTreeTree {

  protected final ExecFormTree execForm;
  protected final ShellFormTree shellForm;

  protected InstructionWithExecOrShellFormTreeImpl(SyntaxToken keyword, @Nullable ExecFormTree execForm, @Nullable ShellFormTree shellForm) {
    super(keyword);
    this.execForm = execForm;
    this.shellForm = shellForm;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(keyword);
    if (execForm != null) {
      result.add(execForm);
    }
    if (shellForm != null) {
      result.add(shellForm);
    }
    return result;
  }

  @Override
  @CheckForNull
  public ExecFormTree execForm() {
    return execForm;
  }

  @Override
  @CheckForNull
  public ShellFormTree shellForm() {
    return shellForm;
  }

  @Override
  public List<SyntaxToken> arguments() {
    List<SyntaxToken> result = new ArrayList<>();
    if (execForm != null) {
      SeparatedList<ExecFormLiteralTree> literals = execForm.literals();
      for (ExecFormLiteralTree element : literals.elements()) {
        result.add(element.value());
      }
    }
    if (shellForm != null) {
      List<SyntaxToken> literals = shellForm.literals();
      result.addAll(literals);
    }
    return result;
  }
}
