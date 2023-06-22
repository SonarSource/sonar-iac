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
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MapSymbolTest {

  TestTree tree = new TestTree();
  CheckContext ctx = mock(CheckContext.class);
  TestMapSymbol present = new TestMapSymbol(ctx, tree, "testSymbol", null);
  TestMapSymbol absent = new TestMapSymbol(ctx, null, "testSymbol", null);

  @Test
  void isPresent() {
    assertThat(present.isPresent()).isTrue();
    assertThat(absent.isPresent()).isFalse();
  }

  @Test
  void isAbsent() {
    assertThat(absent.isAbsent()).isTrue();
    assertThat(present.isAbsent()).isFalse();
  }

  @Test
  void toSecondary() {
    SecondaryLocation secondary = present.toSecondary("secondary");
    assertThat(secondary).isNotNull();
    assertThat(secondary.message).isEqualTo("secondary");
    assertThat(secondary.textRange).isEqualTo(tree.textRange());
    assertThat(absent.toSecondary("secondary")).isNull();
  }

  @Test
  void report() {
    SecondaryLocation secondary = present.toSecondary("secondary");
    present.report("message", secondary);
    verify(ctx, times(1)).reportIssue(eq(tree), eq("message"), anyList());
  }

  @Test
  void reportWhenAbsent() {
    absent.report("message");
    verify(ctx, never()).reportIssue(any(), any(), anyList());
  }

  @Test
  void reportIfAbsent() {
    TestMapSymbol parent = new TestMapSymbol(ctx, tree, "parentSymbol", null);
    TestMapSymbol symbol = new TestMapSymbol(ctx, null, "testSymbol", parent);
    SecondaryLocation secondary = present.toSecondary("secondary");
    symbol.reportIfAbsent("message", secondary);
    verify(ctx, times(1)).reportIssue(eq(tree), eq("message"), anyList());
  }

  @Test
  void reportIfAbsentWhenPresent() {
    present.reportIfAbsent("message");
    verify(ctx, never()).reportIssue(any(), any(), anyList());
  }

  @Test
  void reportIfAbsentWithoutParent() {
    TestMapSymbol symbol = new TestMapSymbol(ctx, null, "testSymbol", null);
    symbol.reportIfAbsent("message");
    verify(ctx, never()).reportIssue(any(), any(), anyList());
  }

  static class TestMapSymbol extends MapSymbol<TestMapSymbol, TestTree> {

    protected TestMapSymbol(CheckContext ctx, @Nullable TestTree tree, String name, @Nullable Symbol<? extends Tree> parent) {
      super(ctx, tree, name, parent);
    }

    @Nullable
    @Override
    protected HasTextRange toHighlight() {
      return tree;
    }
  }

  static class TestTree implements Tree, HasProperties {

    static TextRange DEFAULT_TEXT_RANGE = TextRanges.range(1, 2, 3, 4);

    @Override
    public <T extends PropertyTree> List<T> properties() {
      return Collections.emptyList();
    }

    @Override
    public TextRange textRange() {
      return DEFAULT_TEXT_RANGE;
    }

    @Override
    public List<Tree> children() {
      return Collections.emptyList();
    }
  }

}
