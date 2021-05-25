/*
 * SonarQube IaC Terraform Plugin
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
package org.sonar.plugins.iac.terraform.visitors;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.plugins.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxToken;
import org.sonar.plugins.iac.terraform.tree.impl.LiteralExprTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.ObjectElementTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;

class TreeVisitorTest {

  private TreeVisitor<TreeContext> visitor = new TreeVisitor<>();

  private SyntaxToken identifier = simpleSyntaxToken("var1");
  private SyntaxToken number = simpleSyntaxToken("1");
  private LiteralExprTree numberExpression = new LiteralExprTreeImpl(number);
  private ObjectElementTree objElement = new ObjectElementTreeImpl(identifier, null, numberExpression);

  @Test
  void visit_simple_tree() {
    List<Tree> visited = new ArrayList<>();
    visitor.register(Tree.class, (ctx, tree) -> visited.add(tree));
    visitor.scan(new TreeContext(), objElement);
    assertThat(visited).containsExactly(objElement, identifier, numberExpression, number);
  }

  @Test
  void visit_without_tree() {
    List<Tree> visited = new ArrayList<>();
    visitor.register(Tree.class, (ctx, tree) -> visited.add(tree));
    visitor.scan(new TreeContext(), null);
    assertThat(visited).isEmpty();
  }

  @Test
  void visit_only_literals() {
    List<Tree> visited = new ArrayList<>();
    visitor.register(LiteralExprTree.class, (ctx, tree) -> visited.add(tree));
    visitor.scan(new TreeContext(), objElement);
    assertThat(visited).containsExactly(numberExpression);
  }

  @Test
  void ancestors() {
    Map<Tree, List<Tree>> ancestors = new HashMap<>();
    visitor.register(Tree.class, (ctx, tree) -> ancestors.put(tree, new ArrayList<>(ctx.ancestors())));
    visitor.scan(new TreeContext(), objElement);
    assertThat(ancestors.get(objElement)).isEmpty();
    assertThat(ancestors.get(identifier)).containsExactly(objElement);
    assertThat(ancestors.get(numberExpression)).containsExactly(objElement);
    assertThat(ancestors.get(number)).containsExactly(numberExpression, objElement);
  }

  private SyntaxToken simpleSyntaxToken(String value) {
    return new InternalSyntaxToken(value, null, null);
  }
}
