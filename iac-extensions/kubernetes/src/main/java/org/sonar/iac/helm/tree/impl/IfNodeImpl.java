/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.helm.tree.impl;

import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.IfNodeOrBuilder;
import org.sonar.iac.helm.tree.api.IfNode;
import org.sonar.iac.helm.tree.api.ListNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.PipeNode;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class IfNodeImpl extends AbstractBranchNode implements IfNode {
  public IfNodeImpl(TextRange textRange, PipeNode pipe, ListNode list, ListNode elseList) {
    super(textRange, pipe, list, elseList);
  }

  public static Node fromPb(IfNodeOrBuilder ifNodePb, String source) {
    var pipe = (PipeNode) PipeNodeImpl.fromPb(ifNodePb.getBranchNode().getPipe(), source);
    var list = (ListNode) ListNodeImpl.fromPb(ifNodePb.getBranchNode().getList(), source);
    var elseList = (ListNode) ListNodeImpl.fromPb(ifNodePb.getBranchNode().getElseList(), source);
    return new IfNodeImpl(textRangeFromPb(ifNodePb, source), pipe, list, elseList);
  }
}
