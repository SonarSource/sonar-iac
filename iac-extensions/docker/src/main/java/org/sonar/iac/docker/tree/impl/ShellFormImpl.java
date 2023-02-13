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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ShellForm;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.utils.ArgumentUtils;

public class ShellFormImpl extends AbstractDockerTreeImpl implements ShellForm {

  private final List<Argument> arguments;

  public ShellFormImpl(List<Argument> arguments) {
    this.arguments = arguments;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(arguments);
  }

  @Override
  public Kind getKind() {
    return Kind.SHELL_FORM;
  }

  /**
   * @deprecated To be removed once arguments() methods exist in all implementation and that literals() can be replaced everywhere.
   * For now the method has been transformed to still provide the same data as before.
   */
  @Deprecated(forRemoval = true)
  @Override
  public List<SyntaxToken> literals() {
    List<SyntaxToken> literals = arguments().stream()
      .map(ShellFormImpl::argumentToSyntaxToken)
      .collect(Collectors.toList());
    return literals.contains(null) ? Collections.emptyList() : literals;
  }

  @Nullable
  private static SyntaxToken argumentToSyntaxToken(Argument argument) {
    String value = ArgumentUtils.resolve(argument).value();
    if (value != null) {
      return new SyntaxTokenImpl(value, argument.textRange(), Collections.emptyList());
    }
    return null;
  }

  @Override
  public List<Argument> arguments() {
    return arguments;
  }

  @Override
  public LiteralListType type() {
    return LiteralListType.SHELL;
  }
}
