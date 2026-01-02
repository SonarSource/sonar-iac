/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.common.yaml.tree;

import java.util.Collections;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;

public class ScalarTreeImpl extends YamlTreeImpl implements ScalarTree {

  private final String value;
  private final Style style;

  public ScalarTreeImpl(String value, Style style, YamlTreeMetadata metadata) {
    super(metadata);
    this.value = value;
    this.style = style;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public Style style() {
    return style;
  }

  @Override
  public List<Tree> children() {
    return Collections.emptyList();
  }

  @Override
  public TextRange toHighlight() {
    return metadata().textRange();
  }
}
