/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.docker.tree;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeUtilsTest {

  private final Tree subtree1 = new TestTree("subtree1");
  private final Tree subtree2 = new TestTree("subtree2");
  private final Tree subtree3 = new TestTree("subtree3");
  private final Tree root = new TestTree("root", subtree1, subtree2, subtree3);
  private final Tree nochild = new TestTree("nochild");

  @Test
  public void test_firstDescendant() {
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
  public void test_lastDescendant() {
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

  static class TestTree extends AbstractTestTree {

    private final List<Tree> children;
    private final String name;

    public TestTree(String name, Tree... children) {
      this.name = name;
      this.children = Arrays.asList(children);
    }

    @Override
    public List<Tree> children() {
      return children;
    }

    public String name() {
      return name;
    }
  }
}
