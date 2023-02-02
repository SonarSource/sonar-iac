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
import org.sonar.iac.docker.tree.api.Docker;
import org.sonar.iac.docker.tree.api.DoubleQuotedString;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class DoubleQuotedStringImpl extends AbstractDockerImpl implements DoubleQuotedString {
  private final SyntaxToken leftQuote;
  @Nullable
  private final List<Docker> words;
  private final SyntaxToken rightQuote;

  public DoubleQuotedStringImpl(SyntaxToken leftQuote, @Nullable List<Docker> words, SyntaxToken rightQuote) {
    this.leftQuote = leftQuote;
    this.words = words;
    this.rightQuote = rightQuote;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(leftQuote);
    if (words != null) {
      children.addAll(words);
    }
    children.add(rightQuote);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.DOUBLE_QUOTED_STRING;
  }
}
