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
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ContextualPropertyTreeTest {

  final CheckContext ctx = mock(CheckContext.class);
  final TestPropertyTree tree = mock(TestPropertyTree.class);

  final TestContextualPropertyTree present = new TestContextualPropertyTree(ctx, tree, null);

  final TestContextualPropertyTree absent = new TestContextualPropertyTree(ctx, null, null);

  final Predicate<Tree> allwaysTrue = t -> true;

  @Test
  void reportIfOnPresentProperty() {
    present.reportIf(allwaysTrue, "true");
    present.reportIf(t -> false, "false");
    verify(ctx, times(1)).reportIssue(tree, "true", Collections.emptyList());
  }

  @Test
  void reportIfOnAbsentProperty() {
    absent.reportIf(allwaysTrue, "message");
    absent.reportIf(t -> false, "message");
    verifyNoInteractions(ctx);
  }

  @Test
  void isOnPresentProperty() {
    assertThat(present.is(allwaysTrue)).isTrue();
  }

  @Test
  void isOnAbsentProperty() {
    assertThat(absent.is(allwaysTrue)).isTrue();
  }

  @Test
  void asString() {
    TextTree text = mock(TextTree.class);
    when(text.value()).thenReturn("value");
    when(tree.value()).thenReturn(text);
    TestContextualPropertyTree symbol = new TestContextualPropertyTree(ctx, tree, null);
    assertThat(symbol.asString()).isEqualTo("value");
  }

  private static class TestContextualPropertyTree extends ContextualPropertyTree<TestContextualPropertyTree, TestPropertyTree, Tree> {
    TestContextualPropertyTree(CheckContext ctx, @Nullable TestPropertyTree tree, @Nullable ContextualTree<? extends ContextualTree<?, ?>, ? extends Tree> parent) {
      super(ctx, tree, "test", parent);
    }
  }

  interface TestPropertyTree extends PropertyTree, Tree {

  }

}
