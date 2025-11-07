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
import java.util.function.Supplier;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.CommandNodeOrBuilder;
import org.sonar.iac.helm.tree.api.CommandNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.utils.GoTemplateAstConverter;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class CommandNodeImpl extends AbstractNode implements CommandNode {
  private final List<Node> arguments;

  public CommandNodeImpl(Supplier<TextRange> textRangeSupplier, List<Node> arguments) {
    super(textRangeSupplier);
    this.arguments = Collections.unmodifiableList(arguments);
  }

  public static Node fromPb(CommandNodeOrBuilder nodePb, String source) {
    return new CommandNodeImpl(textRangeFromPb(nodePb, source), GoTemplateAstConverter.unpack(nodePb.getArgsList(), source));
  }

  public List<Node> arguments() {
    return arguments;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(arguments);
  }
}
