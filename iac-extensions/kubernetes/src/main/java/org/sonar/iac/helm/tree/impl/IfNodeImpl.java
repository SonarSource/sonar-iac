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

import java.util.function.Supplier;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.IfNodeOrBuilder;
import org.sonar.iac.helm.tree.api.IfNode;
import org.sonar.iac.helm.tree.api.ListNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.PipeNode;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class IfNodeImpl extends AbstractBranchNode implements IfNode {
  public IfNodeImpl(Supplier<TextRange> textRangeSupplier, PipeNode pipe, ListNode list, ListNode elseList) {
    super(textRangeSupplier, pipe, list, elseList);
  }

  public static Node fromPb(IfNodeOrBuilder ifNodePb, String source) {
    var pipe = (PipeNode) PipeNodeImpl.fromPb(ifNodePb.getBranchNode().getPipe(), source);
    var list = (ListNode) ListNodeImpl.fromPb(ifNodePb.getBranchNode().getList(), source);
    var elseList = (ListNode) ListNodeImpl.fromPb(ifNodePb.getBranchNode().getElseList(), source);
    return new IfNodeImpl(textRangeFromPb(ifNodePb, source), pipe, list, elseList);
  }
}
