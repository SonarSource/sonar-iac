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

import javax.annotation.Nullable;
import org.sonar.iac.helm.protobuf.TemplateNodeOrBuilder;

public class TemplateNode extends AbstractNode {
  @Nullable
  private final String name;
  @Nullable
  private final PipeNode pipe;

  public TemplateNode(long position, @Nullable String name, @Nullable PipeNode pipe) {
    super(position);
    this.name = name;
    this.pipe = pipe;
  }

  public static Node fromPb(TemplateNodeOrBuilder templateNodePb) {
    return new TemplateNode(templateNodePb.getPos(), templateNodePb.getName(), (PipeNode) PipeNode.fromPb(templateNodePb.getPipe()));
  }

  @Override
  public NodeType type() {
    return NodeType.NODE_TEMPLATE;
  }

  @Nullable
  public String name() {
    return name;
  }

  @Nullable
  public PipeNode pipe() {
    return pipe;
  }
}
