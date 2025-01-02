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

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.VariableNodeOrBuilder;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.VariableNode;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class VariableNodeImpl extends AbstractNode implements VariableNode {
  private final List<String> ident;

  public VariableNodeImpl(Supplier<TextRange> textRangeSupplier, List<String> ident) {
    super(textRangeSupplier);
    this.ident = Collections.unmodifiableList(ident);
  }

  public static Node fromPb(VariableNodeOrBuilder nodePb, String source) {
    return new VariableNodeImpl(textRangeFromPb(nodePb, source), nodePb.getIdentList());
  }

  public List<String> idents() {
    return ident;
  }
}
