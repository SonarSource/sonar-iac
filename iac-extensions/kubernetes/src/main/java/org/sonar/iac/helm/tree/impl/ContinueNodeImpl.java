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
package org.sonar.iac.helm.tree.impl;

import java.util.function.Supplier;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.ContinueNodeOrBuilder;
import org.sonar.iac.helm.tree.api.ContinueNode;
import org.sonar.iac.helm.tree.api.Node;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class ContinueNodeImpl extends AbstractNode implements ContinueNode {
  private final long line;

  public ContinueNodeImpl(Supplier<TextRange> textRangeSupplier, long line) {
    super(textRangeSupplier);
    this.line = line;
  }

  public static Node fromPb(ContinueNodeOrBuilder continueNodePb, String source) {
    return new ContinueNodeImpl(textRangeFromPb(continueNodePb, source), continueNodePb.getLine());
  }

  public long line() {
    return line;
  }
}
