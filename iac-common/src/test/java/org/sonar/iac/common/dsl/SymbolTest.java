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
package org.sonar.iac.common.dsl;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SymbolTest {

  final CheckContext ctx = mock(CheckContext.class);
  final Tree tree = mock(Tree.class);

  final Tree parentTree = mock(Tree.class);

  final TestSymbol parent = new TestSymbol(ctx, parentTree, null);

  final TestSymbol present = new TestSymbol(ctx, tree, parent);
  final TestSymbol absent = new TestSymbol(ctx, null, parent);

  @Test
  void report() {
    present.report("present");
    absent.report("absent");

    verify(ctx, times(1)).reportIssue(tree, "present", Collections.emptyList());
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
  }

  @Test
  void reportIfAbsentWithoutParent() {
    TestSymbol symbol = new TestSymbol(ctx, null, null);
    symbol.reportIfAbsent("test");

    verifyNoInteractions(ctx);
  }

  @Test
  void isPresent() {
    assertThat(present.isPresent()).isTrue();
    assertThat(absent.isPresent()).isFalse();
  }

  @Test
  void isAbsent() {
    assertThat(present.isAbsent()).isFalse();
    assertThat(absent.isAbsent()).isTrue();
  }

  static class TestSymbol extends Symbol<TestSymbol, Tree> {

    protected TestSymbol(CheckContext ctx, @Nullable Tree tree, @Nullable Symbol<? extends Symbol<?, ?>, ? extends Tree> parent) {
      super(ctx, tree, "test", parent);
    }
  }

}
