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
import org.sonar.iac.docker.tree.api.ImageTree;
import org.sonar.iac.docker.tree.api.InstructionTree;
import org.sonar.iac.docker.tree.api.ParamTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class FromTreeImpl extends InstructionTreeImpl implements FromTree {

  private final ParamTree platform;
  private final ImageTree image;
  private final AliasTree alias;
  private final List<InstructionTree> instructions;

  public FromTreeImpl(SyntaxToken keyword, @Nullable ParamTree platform, ImageTree image, @Nullable AliasTree alias, List<InstructionTree> instructions) {
    super(keyword);
    this.platform = platform;
    this.image = image;
    this.alias = alias;
    this.instructions = instructions;
  }

  @Nullable
  @Override
  public ParamTree platform() {
    return platform;
  }

  @Override
  public ImageTree image() {
    return image;
  }

  @Nullable
  @Override
  public AliasTree alias() {
    return alias;
  }

  @Override
  public List<InstructionTree> instructions() {
    return instructions;
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
    children.addAll(instructions);
    return children;
  }
  @Override
  public Kind getKind() {
    return Kind.FROM;
  }
}
