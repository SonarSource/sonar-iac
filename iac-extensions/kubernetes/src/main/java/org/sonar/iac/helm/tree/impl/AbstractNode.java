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
package org.sonar.iac.helm.tree.impl;

import java.util.List;
import java.util.function.Supplier;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.tree.api.Node;

public abstract class AbstractNode implements Node {
  private final Supplier<TextRange> textRangeSupplier;

  protected AbstractNode(Supplier<TextRange> textRangeSupplier) {
    this.textRangeSupplier = textRangeSupplier;
  }

  @Override
  public List<Tree> children() {
    return List.of();
  }

  @Override
  public TextRange textRange() {
    return textRangeSupplier.get();
  }
}
