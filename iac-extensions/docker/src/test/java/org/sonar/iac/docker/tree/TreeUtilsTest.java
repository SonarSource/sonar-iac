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
package org.sonar.iac.docker.tree;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.DockerTree;

import static org.assertj.core.api.Assertions.assertThat;

class TreeUtilsTest {

  private final DockerTree subtree1 = new TestTree("subtree1", DockerTree.Kind.DOCKERIMAGE);
  private final DockerTree subtree2 = new TestTree("subtree2", DockerTree.Kind.DOCKERIMAGE);
  private final DockerTree subtree3 = new TestTree("subtree3", DockerTree.Kind.DOCKERIMAGE);
  private final DockerTree root = new TestTree("root", DockerTree.Kind.FILE, subtree1, subtree2, subtree3);
  private final DockerTree nochild = new TestTree("nochild", DockerTree.Kind.FILE);

  @Test
  void shouldRetrieveFirstDescendant() {
    Tree result = TreeUtils.firstDescendant(root, t -> ((TestTree) t).name().contains("subtree")).get();
    assertThat(result).isEqualTo(subtree1);
    result = TreeUtils.firstDescendant(root, t -> ((TestTree) t).name().contains("2")).get();
    assertThat(result).isEqualTo(subtree2);
    result = TreeUtils.firstDescendant(root, t -> ((TestTree) t).name().contains("5")).orElse(null);
    assertThat(result).isNull();
    result = TreeUtils.firstDescendant(nochild, t -> ((TestTree) t).name().contains("subtree")).orElse(null);
    assertThat(result).isNull();
    result = TreeUtils.firstDescendant(null, t -> ((TestTree) t).name().contains("subtree")).orElse(null);
    assertThat(result).isNull();
  }

  @Test
  void shouldRetrieveLastDescendant() {
    Tree result = TreeUtils.lastDescendant(root, t -> ((TestTree) t).name().contains("subtree")).get();
    assertThat(result).isEqualTo(subtree3);
    result = TreeUtils.lastDescendant(root, t -> ((TestTree) t).name().contains("2")).get();
    assertThat(result).isEqualTo(subtree2);
    result = TreeUtils.lastDescendant(root, t -> ((TestTree) t).name().contains("5")).orElse(null);
    assertThat(result).isNull();
    result = TreeUtils.lastDescendant(nochild, t -> ((TestTree) t).name().contains("subtree")).orElse(null);
    assertThat(result).isNull();
    result = TreeUtils.lastDescendant(null, t -> ((TestTree) t).name().contains("subtree")).orElse(null);
    assertThat(result).isNull();
  }

  @Test
  void shouldRetrieveFirstAncestor() {
    DockerTree result = TreeUtils.firstAncestor(subtree1, t -> ((TestTree) t).name().contains("root")).get();
    assertThat(result).isEqualTo(root);
    result = TreeUtils.firstAncestor(subtree1, t -> ((TestTree) t).name().contains("none")).orElse(null);
    assertThat(result).isNull();
    result = TreeUtils.firstAncestor(root, t -> true).orElse(null);
    assertThat(result).isNull();
    result = TreeUtils.firstAncestorOfKind(subtree1, DockerTree.Kind.FILE).get();
    assertThat(result).isEqualTo(root);
    result = TreeUtils.firstAncestorOfKind(subtree1, DockerTree.Kind.CMD).orElse(null);
    assertThat(result).isNull();
  }

  static class TestTree extends AbstractTestTree implements DockerTree {

    private final List<Tree> children;
    private final String name;
    private DockerTree parent;
    private final Kind kind;

    public TestTree(String name, Kind kind, DockerTree... children) {
      this.name = name;
      this.children = Arrays.asList(children);
      this.kind = kind;
      this.children.forEach(child -> ((DockerTree) child).setParent(this));
      this.parent = null;
    }

    @Override
    public List<Tree> children() {
      return children;
    }

    public String name() {
      return name;
    }

    @Override
    public boolean is(Kind... kind) {
      return Arrays.stream(kind).anyMatch(k -> k == this.kind);
    }

    @Override
    public Kind getKind() {
      return kind;
    }

    @Override
    public DockerTree parent() {
      return parent;
    }

    @Override
    public void setParent(DockerTree parent) {
      this.parent = parent;
    }
  }
}
