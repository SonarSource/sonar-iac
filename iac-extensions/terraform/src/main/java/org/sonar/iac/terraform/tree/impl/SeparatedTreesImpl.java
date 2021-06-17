/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
