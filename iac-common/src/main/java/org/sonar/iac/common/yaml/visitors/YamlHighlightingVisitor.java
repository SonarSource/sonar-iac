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
package org.sonar.iac.common.yaml.visitors;

import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

public class YamlHighlightingVisitor extends SyntaxHighlightingVisitor {

  @Override
  protected void languageSpecificHighlighting() {
    register(TupleTree.class, (ctx, tree) -> highlight(tree.key(), KEYWORD));
    register(ScalarTree.class, (ctx, tree) -> ctx.ancestors().stream().findFirst().ifPresent(p -> {
      if (!(p instanceof TupleTree tuple && tuple.key().equals(tree))) {
        highlight(tree, STRING);
      }
    }));
  }

}
