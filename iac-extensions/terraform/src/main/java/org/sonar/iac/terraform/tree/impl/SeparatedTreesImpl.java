/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;

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
