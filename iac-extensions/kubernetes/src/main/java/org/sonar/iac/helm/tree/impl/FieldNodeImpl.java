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

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.FieldNodeOrBuilder;
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.helm.tree.api.Node;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class FieldNodeImpl extends AbstractNode implements FieldNode {
  private final List<String> identifiers;

  public FieldNodeImpl(Supplier<TextRange> textRangeSupplier, List<String> identifiers) {
    super(textRangeSupplier);
    this.identifiers = Collections.unmodifiableList(identifiers);
  }

  public static Node fromPb(FieldNodeOrBuilder nodePb, String source) {
    return new FieldNodeImpl(textRangeFromPb(nodePb, source), nodePb.getIdentList());
  }

  public List<String> identifiers() {
    return identifiers;
  }
}
