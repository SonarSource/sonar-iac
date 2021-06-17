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
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.OneLineBlockTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class OneLineBlockTreeImpl extends TerraformTreeImpl implements OneLineBlockTree {
  private final SyntaxToken type;
  private final List<LabelTree> labels;
  private final SyntaxToken openBrace;
  private final Optional<AttributeTree> attribute;
  private final SyntaxToken closeBrace;

  public OneLineBlockTreeImpl(SyntaxToken type, @Nullable List<LabelTree> labels, SyntaxToken openBrace, @Nullable AttributeTree attribute, SyntaxToken closeBrace) {
    this.type = type;
    this.labels = labels != null ? labels : Collections.emptyList();
    this.openBrace = openBrace;
    this.attribute = Optional.ofNullable(attribute);
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
  public Optional<AttributeTree> attribute() {
    return attribute;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(Arrays.asList(type));
    children.addAll(labels);
    children.add(openBrace);
    if (attribute.isPresent()) {
      children.add(attribute.get());
    }
    children.add(closeBrace);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.ONE_LINE_BLOCK;
  }
}
