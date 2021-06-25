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
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;

public class BlockTreeImpl extends TerraformTreeImpl implements BlockTree {
  private final SyntaxToken type;
  private final List<LabelTree> labels;
  private final SyntaxToken openBrace;
  private final List<TerraformTree> statements;
  private final SyntaxToken closeBrace;
  private Kind kind;

  public BlockTreeImpl(SyntaxToken type, @Nullable List<LabelTree> labels, SyntaxToken openBrace, List<TerraformTree> statements, SyntaxToken closeBrace, Kind kind) {
    this.type = type;
    this.labels = labels != null ? labels : Collections.emptyList();
    this.openBrace = openBrace;
    this.statements = statements;
    this.closeBrace = closeBrace;
    this.kind = kind;
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
  public List<TerraformTree> statements() {
    return statements;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(Arrays.asList(type));
    children.addAll(labels);
    children.add(openBrace);
    children.addAll(statements);
    children.add(closeBrace);
    return children;
  }

  @Override
  public Kind getKind() {
    return kind;
  }
}
