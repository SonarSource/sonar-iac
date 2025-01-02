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
import org.sonar.iac.helm.protobuf.CommentNodeOrBuilder;
import org.sonar.iac.helm.tree.api.CommentNode;
import org.sonar.iac.helm.tree.api.Node;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class CommentNodeImpl extends AbstractNode implements CommentNode {
  private final String value;

  public CommentNodeImpl(Supplier<TextRange> textRangeSupplier, String value) {
    super(textRangeSupplier);
    this.value = value;
  }

  public static Node fromPb(CommentNodeOrBuilder nodePb, String source) {
    return new CommentNodeImpl(textRangeFromPb(nodePb, source), nodePb.getText());
  }

  @Override
  public String value() {
    return value;
  }
}
