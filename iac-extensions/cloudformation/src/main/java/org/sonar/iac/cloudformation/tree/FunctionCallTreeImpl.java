/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.cloudformation.tree;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeImpl;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class FunctionCallTreeImpl extends YamlTreeImpl implements FunctionCallTree {

  private final String name;
  private final Style style;
  private final List<YamlTree> arguments;

  public FunctionCallTreeImpl(String name, Style style, List<YamlTree> arguments, YamlTreeMetadata metadata) {
    super(metadata);
    this.name = name;
    this.style = style;
    this.arguments = arguments;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(arguments);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Style style() {
    return style;
  }

  @Override
  public List<YamlTree> arguments() {
    return arguments;
  }

  @Override
  public TextRange toHighlight() {
    return metadata().textRange();
  }
}
