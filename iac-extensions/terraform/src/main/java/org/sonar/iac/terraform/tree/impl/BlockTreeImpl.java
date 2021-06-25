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
import org.sonar.iac.terraform.api.tree.Statement;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class BlockTreeImpl extends TerraformTreeImpl implements BlockTree {
  private final SyntaxToken identifier;
  private final List<LabelTree> labels;
  private final SyntaxToken openBrace;
  private final List<Statement> statements;
  private final SyntaxToken closeBrace;
  private Kind kind;

  public BlockTreeImpl(SyntaxToken identifier, @Nullable List<LabelTree> labels, SyntaxToken openBrace, List<Statement> statements, SyntaxToken closeBrace, Kind kind) {
    this.identifier = identifier;
    this.labels = labels != null ? labels : Collections.emptyList();
    this.openBrace = openBrace;
    this.statements = statements;
    this.closeBrace = closeBrace;
    this.kind = kind;
  }

  @Override
  public SyntaxToken identifier() {
    return identifier;
  }

  @Override
  public List<LabelTree> labels() {
    return labels;
  }

  @Override
  public List<Statement> statements() {
    return statements;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(Arrays.asList(identifier));
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
