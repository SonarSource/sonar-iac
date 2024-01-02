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
package org.sonar.iac.common.yaml.object;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.YamlTreeTest;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ListObjectTest extends YamlTreeTest {

  CheckContext ctx = mock(CheckContext.class);
  private final TupleTree tree = parseTuple("my_list : [\"my_item\", a]");

  @Test
  void report_fromPresent() {
    ListObject list = ListObject.fromPresent(ctx, tree, "my_list", null);
    assertThat(list.key).isEqualTo("my_list");
    assertThat(list.items).hasSize(2);
    assertThat(list.status).isEqualTo(YamlObject.Status.PRESENT);
    assertNoIssueReported();

    list.report("message");
    assertIssueReported(tree, "message");
  }

  @Test
  void report_fromAbsent() {
    ListObject list = ListObject.fromAbsent(ctx, "unexistent");
    list.report("message");
    assertThat(list.items).isEmpty();
    assertNoIssueReported();
  }

  @Test
  void reportItemIf_fromPresent() {
    TupleTree tree = parseTuple("my_list : [\"my_item\"]");
    ListObject list = ListObject.fromPresent(ctx, tree, "my_list", null);
    assertThat(list.items).hasSize(1);
    list.reportIfAnyItem(e -> true, "message");
    assertIssueReported(tree, "message");
  }

  @Test
  void reportItemIf_fromAbsent() {
    ListObject list = ListObject.fromAbsent(ctx, "my_list");
    list.reportIfAnyItem(e -> true, "message");
    assertNoIssueReported();
  }

  @Test
  void reportItemIf_fromInvalid() {
    YamlTree tree = parseTuple("my_list : not_a_list");
    ListObject list = ListObject.fromPresent(ctx, tree, "my_list", null);
    assertThat(list.items).isEmpty();
    list.reportIfAnyItem(e -> true, "message");
    assertNoIssueReported();
  }

  private void assertNoIssueReported() {
    verify(ctx, never()).reportIssue(any(HasTextRange.class), anyString(), anyList());
  }

  private void assertIssueReported(HasTextRange hasTextRange, String message) {
    verify(ctx).reportIssue(hasTextRange, message);
  }
}
