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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.TreeOrBuilder;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.helm.tree.api.ListNode;

public class GoTemplateTreeImpl implements GoTemplateTree {
  private final String name;
  private final String parseName;
  private final int mode;
  private final ListNode root;

  public GoTemplateTreeImpl(String name, String parseName, int mode, ListNodeImpl root) {
    this.name = name;
    this.parseName = parseName;
    this.mode = mode;
    this.root = root;
  }

  @CheckForNull
  public static GoTemplateTree fromPbTree(@Nullable TreeOrBuilder treePb, String source) {
    if (treePb == null) {
      return null;
    }
    return new GoTemplateTreeImpl(treePb.getName(), treePb.getParseName(), (int) treePb.getMode(), (ListNodeImpl) ListNodeImpl.fromPb(treePb.getRoot(), source));
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String parseName() {
    return parseName;
  }

  @Override
  public int mode() {
    return mode;
  }

  @Override
  public ListNode root() {
    return root;
  }

  @Override
  public TextRange textRange() {
    return root.textRange();
  }
}
