/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
