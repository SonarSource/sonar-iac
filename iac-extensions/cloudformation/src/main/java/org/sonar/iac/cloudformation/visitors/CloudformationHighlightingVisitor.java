/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.cloudformation.visitors;

import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

public class CloudformationHighlightingVisitor extends SyntaxHighlightingVisitor {

  public CloudformationHighlightingVisitor() {
    super();
    register(TupleTree.class, (ctx, tree) -> highlight(tree.key(), KEYWORD));
    register(ScalarTree.class, (ctx, tree) -> {
      Tree parent = ctx.ancestors().getFirst();
      if (!(parent instanceof TupleTree && ((TupleTree) parent).key().equals(tree))) {
        highlight(tree, STRING);
      }
    });
  }
}
