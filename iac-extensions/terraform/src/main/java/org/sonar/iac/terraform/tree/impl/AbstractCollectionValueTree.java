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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;

public abstract class AbstractCollectionValueTree<T extends TerraformTree> extends TerraformTreeImpl {
  private final SyntaxToken openBrace;
  private final SeparatedTrees<T> elements;
  private final SyntaxToken closeBrace;

  AbstractCollectionValueTree(SyntaxToken openBrace, @Nullable SeparatedTrees<T> elements, SyntaxToken closeBrace) {
    this.openBrace = openBrace;
    this.elements = elements != null ? elements : SeparatedTreesImpl.empty();
    this.closeBrace = closeBrace;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(Arrays.asList(openBrace));
    children.addAll(elements.treesAndSeparators());
    children.add(closeBrace);
    return children;
  }

  public SeparatedTrees<T> elements() {
    return elements;
  }
}
