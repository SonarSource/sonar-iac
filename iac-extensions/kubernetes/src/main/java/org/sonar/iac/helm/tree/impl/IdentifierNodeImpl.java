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

import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.IdentifierNodeOrBuilder;
import org.sonar.iac.helm.tree.api.IdentifierNode;
import org.sonar.iac.helm.tree.api.Node;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class IdentifierNodeImpl extends AbstractNode implements IdentifierNode {
  @Nullable
  private final String identifier;

  public IdentifierNodeImpl(Supplier<TextRange> textRangeSupplier, String identifier) {
    super(textRangeSupplier);
    this.identifier = identifier;
  }

  public static Node fromPb(IdentifierNodeOrBuilder identifierNodePb, String source) {
    return new IdentifierNodeImpl(textRangeFromPb(identifierNodePb, source), identifierNodePb.getIdent());
  }

  public String identifier() {
    return identifier;
  }

}
