/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.checkdsl;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ContextualListTreeTest {

  final CheckContext ctx = mock(CheckContext.class);
  final ContextualPropertyTreeTest.TestPropertyTree tree = mock(ContextualPropertyTreeTest.TestPropertyTree.class);
  final TestContextualListTree present = new TestContextualListTree(ctx, tree, "presentName", null, List.of(tree));
  final TestContextualListTree absent = new TestContextualListTree(ctx, null, "absentName", null, Collections.emptyList());
  final TestContextualListTree empty = new TestContextualListTree(ctx, tree, "emptyName", null, Collections.emptyList());

  final Predicate<Tree> allwaysTrue = t -> true;

  @Test
  void reportItemIf() {
    present.reportItemIf(allwaysTrue, "presentMsg");
    empty.reportItemIf(allwaysTrue, "emptyMsg");
    absent.reportItemIf(allwaysTrue, "absentMsg");

    verify(ctx, times(1)).reportIssue(tree, "presentMsg", Collections.emptyList());
    verify(ctx, never()).reportIssue(tree, "emptyMsg", Collections.emptyList());
    verify(ctx, never()).reportIssue(tree, "absentMsg", Collections.emptyList());
  }

  @Test
  void getItemIf() {
    assertThat(present.getItemIf(allwaysTrue)).isNotEmpty();
    assertThat(empty.getItemIf(allwaysTrue)).isEmpty();
    assertThat(absent.getItemIf(allwaysTrue)).isEmpty();
  }

  @Test
  void isEmpty() {
    assertThat(present.isEmpty()).isFalse();
    assertThat(empty.isEmpty()).isTrue();
    assertThat(absent.isEmpty()).isFalse();
  }

  @Test
  void reportIfEmpty() {
    present.reportIfEmpty("presentMsg");
    empty.reportIfEmpty("emptyMsg");
    absent.reportIfEmpty("absentMsg");

    verify(ctx, times(1)).reportIssue(tree, "emptyMsg", Collections.emptyList());
    verify(ctx, never()).reportIssue(tree, "presentMsg", Collections.emptyList());
    verify(ctx, never()).reportIssue(tree, "absentMsg", Collections.emptyList());
  }

  static class TestContextualListTree extends ContextualListTree<TestContextualListTree, Tree, Tree> {
    protected TestContextualListTree(CheckContext ctx, @Nullable Tree tree, String name, @Nullable ContextualTree<?, ?> parent, List<Tree> items) {
      super(ctx, tree, name, parent, items);
    }
  }

}
