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

import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.KeyValuePairTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class KeyValuePairTreeImpl extends DockerTreeImpl implements KeyValuePairTree {
  private final SyntaxToken key;
  private final SyntaxToken value;
  private final SyntaxToken equals;

  public KeyValuePairTreeImpl(SyntaxToken key, SyntaxToken value) {
    this.key = key;
    this.equals = null;
    this.value = value;
  }

  public KeyValuePairTreeImpl(SyntaxToken key, SyntaxToken equals, SyntaxToken value) {
    this.key = key;
    this.equals = equals;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    if (equals != null) {
      return List.of(key, equals, value);
    } else {
      return List.of(key, value);
    }
  }

  @Override
  public Kind getKind() {
    return Kind.KEYVALUEPAIR;
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
