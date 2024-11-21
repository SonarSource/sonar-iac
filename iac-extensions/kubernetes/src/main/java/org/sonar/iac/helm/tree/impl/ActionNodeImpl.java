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

import java.util.List;
import java.util.function.Supplier;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.ActionNodeOrBuilder;
import org.sonar.iac.helm.tree.api.ActionNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.PipeNode;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class ActionNodeImpl extends AbstractNode implements ActionNode {
  private final PipeNode pipe;

  public ActionNodeImpl(Supplier<TextRange> textRangeSupplier, PipeNode pipe) {
    super(textRangeSupplier);
    this.pipe = pipe;
  }

  public static Node fromPb(ActionNodeOrBuilder nodePb, String source) {
    return new ActionNodeImpl(textRangeFromPb(nodePb, source), (PipeNode) PipeNodeImpl.fromPb(nodePb.getPipe(), source));
  }

  public PipeNode pipe() {
    return pipe;
  }

  @Override
  public List<Tree> children() {
    return List.of(pipe);
  }
}
