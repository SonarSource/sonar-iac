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
package org.sonar.iac.helm.tree;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.iac.helm.protobuf.ChainNodeOrBuilder;
import org.sonar.iac.helm.tree.utils.GoTemplateAstConverter;

public class ChainNode extends AbstractNode {
  @Nullable
  private final Node node;
  private final List<String> field;

  public ChainNode(long position, @Nullable Node node, List<String> field) {
    super(position);
    this.node = node;
    this.field = Collections.unmodifiableList(field);
  }

  public static Node fromPb(ChainNodeOrBuilder chainNodePb) {
    return new ChainNode(chainNodePb.getPos(), Optional.ofNullable(chainNodePb.getNode()).map(GoTemplateAstConverter::unpackNode).orElse(null), chainNodePb.getFieldList());
  }

  @Override
  public NodeType type() {
    return NodeType.NODE_CHAIN;
  }

  public Optional<Node> node() {
    return Optional.ofNullable(node);
  }

  public List<String> field() {
    return field;
  }
}
