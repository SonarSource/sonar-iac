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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.helm.tree.api.BranchNode;
import org.sonar.iac.helm.tree.api.ListNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.PipeNode;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstHelper.addChildrenIfPresent;

public abstract class AbstractBranchNode extends AbstractNode implements BranchNode {
  @Nullable
  private final PipeNode pipe;
  @Nullable
  private final ListNode list;
  @Nullable
  private final ListNode elseList;

  protected AbstractBranchNode(long position, long length, @Nullable PipeNode pipe, @Nullable ListNode list, @Nullable ListNode elseList) {
    super(position, length);
    this.pipe = pipe;
    this.list = list;
    this.elseList = elseList;
  }

  @CheckForNull
  public PipeNode pipe() {
    return pipe;
  }

  @CheckForNull
  public ListNode list() {
    return list;
  }

  @CheckForNull
  public ListNode elseList() {
    return elseList;
  }

  @Override
  public List<Node> children() {
    List<Node> children = new ArrayList<>();
    addChildrenIfPresent(children, pipe);
    addChildrenIfPresent(children, list);
    addChildrenIfPresent(children, elseList);
    return children;
  }
}
