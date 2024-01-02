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
package org.sonar.iac.arm.checkdsl;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ContextualMapTest {

  TestTree tree = new TestTree();
  CheckContext ctx = mock(CheckContext.class);
  TestContextualMap present = new TestContextualMap(ctx, tree, "test", null);
  TestContextualMap absent = new TestContextualMap(ctx, null, "test", null);

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
    verifyNoInteractions(ctx);
  }

  @Test
  void reportIfAbsent() {
    TestContextualMap parent = new TestContextualMap(ctx, tree, "parent", null);
    TestContextualMap contextualMap = new TestContextualMap(ctx, null, "test", parent);
    SecondaryLocation secondary = present.toSecondary("secondary");
    contextualMap.reportIfAbsent("message", secondary);
    verify(ctx, times(1)).reportIssue(tree, "message", List.of(secondary));
  }

  @Test
  void reportIfAbsentWhenPresent() {
    present.reportIfAbsent("message");
    verifyNoInteractions(ctx);
  }

  @Test
  void reportIfAbsentWithoutParent() {
    TestContextualMap contextualMap = new TestContextualMap(ctx, null, "testSymbol", null);
    contextualMap.reportIfAbsent("message");
    verifyNoInteractions(ctx);
  }

  static class TestContextualMap extends ContextualMap<TestContextualMap, TestTree> {
    protected TestContextualMap(CheckContext ctx, @Nullable TestTree tree, @Nullable String name, @Nullable ContextualMap<?, ?> parent) {
      super(ctx, tree, name, parent);
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
