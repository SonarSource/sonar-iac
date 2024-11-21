/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.helm.protobuf.StringNodeOrBuilder;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.StringNode;
import org.sonar.iac.helm.tree.utils.GoTemplateAstConverter;

public class StringNodeImpl extends AbstractNode implements StringNode {
  private final String text;

  public StringNodeImpl(Supplier<TextRange> textRangeSupplier, String text) {
    super(textRangeSupplier);
    this.text = text;
  }

  public static Node fromPb(StringNodeOrBuilder nodePb, String source) {
    return new StringNodeImpl(GoTemplateAstConverter.textRangeFromPb(nodePb, source), nodePb.getText());
  }

  public String text() {
    return text;
  }
}
