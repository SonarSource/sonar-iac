/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.BodyTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class BlockTreeImpl extends TerraformTreeImpl implements BlockTree {
  private final SyntaxToken type;
  private final List<LabelTree> labels;
  private final SyntaxToken openBrace;
  private final Optional<BodyTree> body;
  private final SyntaxToken closeBrace;

  public BlockTreeImpl(SyntaxToken type, @Nullable List<LabelTree> labels, SyntaxToken openBrace, @Nullable BodyTree body, SyntaxToken closeBrace) {
    this.type = type;
    this.labels = labels != null ? labels : Collections.emptyList();
    this.openBrace = openBrace;
    this.body = Optional.ofNullable(body);
    this.closeBrace = closeBrace;
  }

  @Override
  public SyntaxToken type() {
    return type;
  }

  @Override
  public List<LabelTree> labels() {
    return labels;
  }

  @Override
  public Optional<BodyTree> body() {
    return body;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(Arrays.asList(type));
    children.addAll(labels);
    children.add(openBrace);
    body.ifPresent(children::add);
    children.add(closeBrace);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.BLOCK;
  }
}
