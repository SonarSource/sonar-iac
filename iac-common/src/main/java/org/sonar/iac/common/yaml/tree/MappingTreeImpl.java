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
package org.sonar.iac.common.yaml.tree;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;

public class MappingTreeImpl extends YamlTreeImpl implements MappingTree {
  private final List<TupleTree> elements;

  public MappingTreeImpl(List<TupleTree> elements, YamlTreeMetadata metadata) {
    super(metadata);
    this.elements = elements;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(elements);
  }

  @Override
  public List<TupleTree> elements() {
    return elements;
  }

  @Override
  public List<TupleTree> properties() {
    return elements;
  }

  @Override
  public TextRange toHighlight() {
    if (elements != null && !elements.isEmpty()) {
      return elements.get(0).toHighlight();
    }
    return metadata().textRange();
  }
}
