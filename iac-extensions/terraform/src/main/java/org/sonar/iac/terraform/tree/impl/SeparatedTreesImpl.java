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

import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeparatedTreesImpl<T extends TerraformTree> implements SeparatedTrees<T> {

  private final List<T> trees;
  private final List<SyntaxToken> separators;
  private final List<TerraformTree> elementsAndSeparators;

  public SeparatedTreesImpl(List<T> trees, List<SyntaxToken> separators) {
    if (!(trees.size() == separators.size() + 1 || trees.size() == separators.size())) {
      throw new IllegalArgumentException(String.format("Instantiating SeparatedTrees with inconsistent number of elements (%s) and separators (%s)",
        trees.size(), separators.size()));
    }

    this.trees = trees;
    this.separators = separators;

    elementsAndSeparators = new ArrayList<>();
    int separatorsSize = separators.size();
    for (int i = 0; i < trees.size(); i++) {
      elementsAndSeparators.add(trees.get(i));
      if (i < separatorsSize) {
        elementsAndSeparators.add(separators.get(i));
      }
    }
  }

  public static <T extends TerraformTree> SeparatedTreesImpl<T> empty() {
    return new SeparatedTreesImpl<>(Collections.emptyList(), Collections.emptyList());
  }

  @Override
  public List<T> trees() {
    return trees;
  }

  @Override
  public List<SyntaxToken> separators() {
    return separators;
  }

  @Override
  public List<TerraformTree> treesAndSeparators() {
    return elementsAndSeparators;
  }

}
