/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.tree.impl;

import org.sonar.iac.terraform.api.tree.BodyTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileTreeImpl extends TerraformTreeImpl implements FileTree {
  private final Optional<BodyTree> body;
  private final SyntaxToken eof;

  public FileTreeImpl(BodyTree body, SyntaxToken eof) {
    this.body = Optional.ofNullable(body);
    this.eof = eof;
  }

  @Override
  public Optional<BodyTree> body() {
    return body;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    body.ifPresent(children::add);
    children.add(eof);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.FILE;
  }
}
