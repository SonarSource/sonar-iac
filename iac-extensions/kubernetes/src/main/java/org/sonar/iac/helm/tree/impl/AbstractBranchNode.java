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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.tree.api.BranchNode;
import org.sonar.iac.helm.tree.api.ListNode;
import org.sonar.iac.helm.tree.api.PipeNode;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstHelper.addChildrenIfPresent;

public abstract class AbstractBranchNode extends AbstractNode implements BranchNode {
  @Nullable
  private final PipeNode pipe;
  @Nullable
  private final ListNode list;
  @Nullable
  private final ListNode elseList;

  protected AbstractBranchNode(Supplier<TextRange> textRangeSupplier, @Nullable PipeNode pipe, @Nullable ListNode list, @Nullable ListNode elseList) {
    super(textRangeSupplier);
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
  public List<Tree> children() {
    var children = new ArrayList<Tree>();
    addChildrenIfPresent(children, pipe);
    addChildrenIfPresent(children, list);
    addChildrenIfPresent(children, elseList);
    return children;
  }
}
