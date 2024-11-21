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
package org.sonar.iac.common.yaml.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

public class SequenceTreeImpl extends YamlTreeImpl implements SequenceTree {
  private final List<YamlTree> elements;

  public SequenceTreeImpl(List<YamlTree> elements, YamlTreeMetadata metadata) {
    super(metadata);
    this.elements = elements;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(elements);
  }

  @Override
  public List<YamlTree> elements() {
    return elements;
  }

  @Override
  public Iterator<YamlTree> iterator() {
    return elements.iterator();
  }

  @Override
  public TextRange toHighlight() {
    return TextRanges.merge(elements.stream()
      .map(HasTextRange::textRange)
      .toList());
  }
}
