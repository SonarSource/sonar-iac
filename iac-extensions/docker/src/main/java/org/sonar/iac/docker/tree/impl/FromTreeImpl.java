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
import org.sonar.iac.docker.tree.api.AliasTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.KeyValuePairTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class FromTreeImpl extends InstructionTreeImpl implements FromTree {

  private final KeyValuePairTree platform;
  private final SyntaxToken image;
  private final AliasTree alias;

  public FromTreeImpl(SyntaxToken keyword, @Nullable KeyValuePairTree platform, SyntaxToken image, @Nullable AliasTree alias) {
    super(keyword);
    this.platform = platform;
    this.image = image;
    this.alias = alias;
  }

  @Nullable
  @Override
  public KeyValuePairTree platform() {
    return platform;
  }

  @Override
  public SyntaxToken image() {
    return image;
  }

  @Nullable
  @Override
  public AliasTree alias() {
    return alias;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(keyword);
    if (platform != null) {
      children.add(platform);
    }
    children.add(image);
    if (alias != null) {
      children.add(alias);
    }
    return children;
  }
  @Override
  public Kind getKind() {
    return Kind.FROM;
  }
}
