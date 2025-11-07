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
package org.sonar.iac.arm.tree.impl.json;

import java.util.Collections;
import java.util.List;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class ArrayExpressionImpl extends AbstractArmTreeImpl implements ArrayExpression {
  private final List<Expression> elements;
  private final YamlTreeMetadata metadata;

  public ArrayExpressionImpl(YamlTreeMetadata metadata, List<Expression> elements) {
    this.metadata = metadata;
    this.elements = elements;
  }

  @Override
  public TextRange textRange() {
    return metadata.textRange();
  }

  @Override
  public List<Tree> children() {
    return Collections.unmodifiableList(elements);
  }

  @Override
  public List<Expression> elements() {
    return elements;
  }
}
