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
package org.sonar.iac.common.checkdsl;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ContextualTreeTest {

  final CheckContext ctx = mock(CheckContext.class);
  final Tree tree = mock(Tree.class);

  final Tree parentTree = mock(Tree.class);

  final TestContextualTree parent = new TestContextualTree(ctx, parentTree, null);

  final TestContextualTree present = new TestContextualTree(ctx, tree, parent);
  final TestContextualTree absent = new TestContextualTree(ctx, null, parent);

  @Test
  void report() {
    present.report("present");
    absent.report("absent");

    verify(ctx, times(1)).reportIssue(tree, "present", Collections.emptyList());
    verify(ctx, never()).reportIssue(tree, "absent", Collections.emptyList());
  }

  @Test
  void reportWithSecondary() {
    SecondaryLocation secondary = mock(SecondaryLocation.class);
    present.report("message", secondary);
    verify(ctx, times(1)).reportIssue(tree, "message", List.of(secondary));
  }

  @Test
  void toSecondary() {
    TextRange textRange = mock(TextRange.class);
    when(tree.textRange()).thenReturn(textRange);

    SecondaryLocation presentSecondary = present.toSecondary("secondary");

    assertThat(presentSecondary).isNotNull();
    assertThat(presentSecondary.message).isEqualTo("secondary");
    assertThat(presentSecondary.textRange).isEqualTo(textRange);

    assertThat(absent.toSecondary("secondary")).isNull();
  }

  @Test
  void reportIfAbsent() {
    present.reportIfAbsent("present");
    absent.reportIfAbsent("absent");

    verify(ctx, times(1)).reportIssue(parentTree, "absent", Collections.emptyList());
    verify(ctx, never()).reportIssue(parentTree, "present", Collections.emptyList());
  }

  @Test
  void reportIfAbsentWithoutParent() {
    TestContextualTree symbol = new TestContextualTree(ctx, null, null);
    symbol.reportIfAbsent("test");

    verifyNoInteractions(ctx);
  }

  @Test
  void isPresent() {
    assertThat(present.isPresent()).isTrue();
    assertThat(absent.isPresent()).isFalse();
  }

  @Test
  void ifPresent() {
    Consumer<Tree> consumerPresent = mock(Consumer.class);
    Consumer<Tree> consumerAbsent = mock(Consumer.class);
    present.ifPresent(consumerPresent);
    absent.ifPresent(consumerAbsent);
    verify(consumerPresent, times(1)).accept(any());
    verify(consumerAbsent, never()).accept(any());
  }

  @Test
  void isAbsent() {
    assertThat(present.isAbsent()).isFalse();
    assertThat(absent.isAbsent()).isTrue();
  }

  static class TestContextualTree extends ContextualTree<TestContextualTree, Tree> {

    protected TestContextualTree(CheckContext ctx, @Nullable Tree tree, @Nullable ContextualTree<? extends ContextualTree<?, ?>, ? extends Tree> parent) {
      super(ctx, tree, "test", parent);
    }
  }

}
