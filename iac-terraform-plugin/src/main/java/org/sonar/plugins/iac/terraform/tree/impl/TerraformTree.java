/*
 * SonarQube IaC Terraform Plugin
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
package org.sonar.plugins.iac.terraform.tree.impl;

import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;

import java.util.List;

public abstract class TerraformTree implements Tree {
  private SyntaxToken lastToken = null;

  /**
   * Creates iterator for children of this node.
   * Note that iterator may contain {@code null} elements.
   *
   * @throws UnsupportedOperationException if {@link #isLeaf()} returns {@code true}
   */
  public abstract List<Tree> children();

  public boolean isLeaf() {
    //TODO: where should this be set to true
    return false;
  }

  public SyntaxToken getLastToken() {
    if (lastToken == null) {
      for (Tree tree : children()) {
        TerraformTree child = (TerraformTree) tree;
        if (child != null) {
          SyntaxToken childLastToken = child.getLastToken();
          if (childLastToken != null) {
            lastToken = childLastToken;
          }
        }
      }
    }
    return lastToken;
  }
}
