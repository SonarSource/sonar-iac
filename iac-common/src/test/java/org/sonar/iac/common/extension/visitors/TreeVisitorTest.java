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
package org.sonar.iac.common.extension.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;

import static org.assertj.core.api.Assertions.assertThat;

class TreeVisitorTest {

  private final TreeVisitor<TreeContext> visitor = new TreeVisitor<>();

  private final SubTestTree subtree1 = new SubTestTree();
  private final SubTestTree subtree2 = new SubTestTree();
  private final SubTestTree subtree3 = new SubTestTree();
  private final Tree tree1 = new TestTree(subtree1, subtree2);
  private final Tree tree2 = new TestTree(subtree3);
  private final Tree root = new TestTree(tree1, tree2);

  @Test
  void visit_simple_tree() {
    List<Tree> visited = new ArrayList<>();
    visitor.register(Tree.class, (ctx, tree) -> visited.add(tree));
    visitor.scan(new TreeContext(), root);
    assertThat(visited).containsOnly(root, tree1, tree2, subtree1, subtree2, subtree3);
  }

  @Test
  void visit_without_tree() {
    List<Tree> visited = new ArrayList<>();
    visitor.register(Tree.class, (ctx, tree) -> visited.add(tree));
    visitor.scan(new TreeContext(), null);
    assertThat(visited).isEmpty();
  }

  @Test
  void visit_sub_tree() {
    List<Tree> visited = new ArrayList<>();
    visitor.register(SubTestTree.class, (ctx, tree) -> visited.add(tree));
    visitor.scan(new TreeContext(), root);
    assertThat(visited).containsOnly(subtree1, subtree2, subtree3);
  }

  @Test
  void visit_tree_with_null_child() {
    List<Tree> visited = new ArrayList<>();
    visitor.register(Tree.class, (ctx, tree) -> visited.add(tree));
    Tree tree = new TestTree((Tree) null);
    visitor.scan(new TreeContext(), tree);
    assertThat(visited).containsOnly(tree);
  }

  @Test
  void ancestors() {
    Map<Tree, List<Tree>> ancestors = new HashMap<>();
    visitor.register(Tree.class, (ctx, tree) -> ancestors.put(tree, new ArrayList<>(ctx.ancestors())));
    visitor.scan(new TreeContext(), root);
    assertThat(ancestors.get(root)).isEmpty();
    assertThat(ancestors.get(tree1)).containsExactly(root);
    assertThat(ancestors.get(subtree1)).containsExactly(tree1, root);
    assertThat(ancestors.get(subtree2)).containsExactly(tree1, root);
  }

  static class TestTree extends AbstractTestTree {

    private final List<Tree> children;

    public TestTree(Tree... children) {
      this.children = Arrays.asList(children);
    }

    @Override
    public TextRange textRange() {
      return null;
    }

    @Override
    public List<Tree> children() {
      return children;
    }
  }

  static class SubTestTree extends TestTree {
    public SubTestTree() {
      super();
    }
  }
}
