/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
  public SyntaxToken identifier() {
    return key;
  }

  @Override
  public List<LabelTree> labels() {
    return labels;
  }

  @Override
  public List<StatementTree> statements() {
    return body.statements();
  }

  @Override
  public SyntaxToken key() {
    return key;
  }

  @Override
  public Tree value() {
    return body;
  }

  @Override
  public BodyTree body() {
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
