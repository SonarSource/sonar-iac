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
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.NewKeyValuePair;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class NewKeyValuePairImpl extends AbstractDockerTreeImpl implements NewKeyValuePair {

  private final Argument key;
  @Nullable
  private final SyntaxToken equalSign;
  @Nullable
  private final Argument value;

  public NewKeyValuePairImpl(Argument key, @Nullable SyntaxToken equalSign, @Nullable Argument value) {
    this.key = key;
    this.equalSign = equalSign;
    this.value = value;
  }

  @Override
  public Argument key() {
    return key;
  }

  @Nullable
  @Override
  public SyntaxToken equalSign() {
    return equalSign;
  }

  @Nullable
  @Override
  public Argument value() {
    return value;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(key);
    if (equalSign != null) {
      children.add(equalSign);
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
}
