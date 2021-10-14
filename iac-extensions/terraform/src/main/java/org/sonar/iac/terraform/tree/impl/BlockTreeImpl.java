/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.BodyTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.StatementTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class BlockTreeImpl extends TerraformTreeImpl implements BlockTree {
  private final SyntaxToken key;
  private final List<LabelTree> labels;
  private final BodyTree body;
  private Kind kind;

  public BlockTreeImpl(SyntaxToken key, @Nullable List<LabelTree> labels, BodyTree body, Kind kind) {
    this.key = key;
    this.labels = labels != null ? labels : Collections.emptyList();
    this.body = body;
    this.kind = kind;
  }

  @Override
  public List<LabelTree> labels() {
    return labels;
  }

  @Override
  public List<StatementTree> properties() {
    return body.statements();
  }

  @Override
  public SyntaxToken key() {
    return key;
  }

  @Override
  public BodyTree value() {
    return body;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(key);
    children.addAll(labels);
    children.add(body);
    return children;
  }

  @Override
  public Kind getKind() {
    return kind;
  }
}
