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
import java.util.stream.Collectors;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.tree.api.Docker;

public abstract class AbstractDockerImpl implements Docker {

  protected TextRange textRange;
  protected Docker parent;

  @Override
  public final boolean is(Kind... kind) {
    for (Kind kindIter : kind) {
      if (getKind() == kindIter) {
        return true;
      }
    }
    return false;
  }

  @Override
  public TextRange textRange() {
    if (textRange == null) {
      List<TextRange> childRanges = children().stream().map(HasTextRange::textRange).collect(Collectors.toList());
      textRange = TextRanges.merge(childRanges);
    }
    return textRange;
  }

  @Override
  public Docker parent() {
    return parent;
  }

  @Override
  public void setParent(Docker parent) {
    this.parent = parent;
  }
}
