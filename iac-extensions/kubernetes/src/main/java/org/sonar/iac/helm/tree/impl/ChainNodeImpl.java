/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.ChainNodeOrBuilder;
import org.sonar.iac.helm.tree.api.ChainNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.utils.GoTemplateAstConverter;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstHelper.addChildrenIfPresent;

public class ChainNodeImpl extends AbstractNode implements ChainNode {
  @Nullable
  private final Node node;
  private final List<String> field;

  public ChainNodeImpl(Supplier<TextRange> textRangeSupplier, @Nullable Node node, List<String> field) {
    super(textRangeSupplier);
    this.node = node;
    this.field = Collections.unmodifiableList(field);
  }

  public static Node fromPb(ChainNodeOrBuilder chainNodePb, String source) {
    return new ChainNodeImpl(
      GoTemplateAstConverter.textRangeFromPb(chainNodePb, source),
      Optional.ofNullable(chainNodePb.getNode()).map(it -> GoTemplateAstConverter.unpackNode(it, source)).orElse(null),
      chainNodePb.getFieldList());
  }

  public Optional<Node> node() {
    return Optional.ofNullable(node);
  }

  public List<String> fields() {
    return field;
  }

  @Override
  public List<Tree> children() {
    var children = new ArrayList<Tree>();
    addChildrenIfPresent(children, node);
    return children;
  }
}
