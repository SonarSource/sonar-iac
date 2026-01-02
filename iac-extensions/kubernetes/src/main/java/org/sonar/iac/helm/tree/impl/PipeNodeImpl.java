/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.PipeNodeOrBuilder;
import org.sonar.iac.helm.tree.api.CommandNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.PipeNode;
import org.sonar.iac.helm.tree.api.VariableNode;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class PipeNodeImpl extends AbstractNode implements PipeNode {
  private final List<VariableNode> declarations;
  private final List<CommandNode> commands;

  public PipeNodeImpl(Supplier<TextRange> textRangeSupplier, List<VariableNode> declarations, List<CommandNode> commands) {
    super(textRangeSupplier);
    this.declarations = Collections.unmodifiableList(declarations);
    this.commands = Collections.unmodifiableList(commands);
  }

  public static Node fromPb(PipeNodeOrBuilder nodePb, String source) {
    return new PipeNodeImpl(
      textRangeFromPb(nodePb, source),
      nodePb.getDeclList().stream().map(node -> (VariableNode) VariableNodeImpl.fromPb(node, source)).toList(),
      nodePb.getCmdsList().stream().map(node -> (CommandNode) CommandNodeImpl.fromPb(node, source)).toList());
  }

  public List<VariableNode> declarations() {
    return declarations;
  }

  public List<CommandNode> commands() {
    return commands;
  }

  @Override
  public List<Tree> children() {
    var children = new ArrayList<Tree>();
    children.addAll(declarations);
    children.addAll(commands);
    return children;
  }
}
