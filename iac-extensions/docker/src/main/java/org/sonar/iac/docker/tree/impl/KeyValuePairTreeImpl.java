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
import org.sonar.iac.docker.tree.api.KeyValuePairTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class KeyValuePairTreeImpl extends DockerTreeImpl implements KeyValuePairTree {
  private final SyntaxToken prefix;
  private final SyntaxToken key;
  private final SyntaxToken value;
  private final SyntaxToken equals;

  public KeyValuePairTreeImpl(@Nullable SyntaxToken prefix, SyntaxToken key, @Nullable SyntaxToken equals, @Nullable SyntaxToken value) {
    this.prefix = prefix;
    this.key = key;
    this.equals = equals;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    if (prefix != null) {
      children.add(prefix);
    }
    children.add(key);
    if (equals != null) {
      children.add(equals);
    }
    if (value != null) {
      children.add(value);
    }
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.KEY_VALUE_PAIR;
  }

  @Override
  public SyntaxToken prefix() {
    return prefix;
  }

  @Override
  public SyntaxToken key() {
    return key;
  }

  @Override
  public SyntaxToken equals() {
    return equals;
  }

  @Override
  public SyntaxToken value() {
    return value;
  }
}
