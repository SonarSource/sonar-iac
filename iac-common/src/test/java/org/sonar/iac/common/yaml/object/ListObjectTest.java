/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.common.yaml.object;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.yaml.YamlTreeTest;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
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
  private final TupleTree tupleTree = parseTuple("my_list : [\"my_item\", a]");
  private final SequenceTree sequenceTree = parse("""
    - item_1
    - item_2
    """, SequenceTree.class);

  @Test
  void shouldReportOnItemsFromPresent() {
    ListObject list = ListObject.fromPresent(ctx, tupleTree, "my_list", null);
    assertThat(list.key).isEqualTo("my_list");
    assertThat(list.items).hasSize(2);
    assertThat(list.status).isEqualTo(YamlObject.Status.PRESENT);

    list.reportOnItems("message");
    var merged = TextRanges.merge(list.items.stream().map(HasTextRange::textRange).toList());
    assertIssueReported(merged, "message");
  }

  @Test
  void shouldReportOnItemsFromPresentArray() {
    ListObject list = ListObject.fromPresent(ctx, sequenceTree, null, null);
    assertThat(list.key).isNull();
    assertThat(list.items).hasSize(2);
    assertThat(list.status).isEqualTo(YamlObject.Status.PRESENT);

    list.reportOnItems("message");
    var merged = TextRanges.merge(list.items.stream().map(HasTextRange::textRange).toList());
    assertIssueReported(merged, "message");
  }

  @Test
  void shouldNotReportFromAbsent() {
    ListObject list = ListObject.fromAbsent(ctx, "unexistent");
    list.reportOnItems("message");
    assertThat(list.items).isEmpty();
    assertNoIssueReported();
  }

  @Test
  void shouldReportItemIfFromPresent() {
    TupleTree tree = parseTuple("my_list : [\"my_item\"]");
    ListObject list = ListObject.fromPresent(ctx, tree, "my_list", null);
    assertThat(list.items).hasSize(1);
    list.reportIfAnyItem(e -> true, "message");
    assertIssueReported(((SequenceTree) tree.value()).elements().get(0).textRange(), "message");
  }

  @Test
  void shouldNotReportItemIfFromAbsent() {
    ListObject list = ListObject.fromAbsent(ctx, "my_list");
    list.reportIfAnyItem(e -> true, "message");
    assertNoIssueReported();
  }

  @Test
  void shouldNotReportItemIfFromInvalid() {
    YamlTree tree = parseTuple("my_list : not_a_list");
    ListObject list = ListObject.fromPresent(ctx, tree, "my_list", null);
    assertThat(list.items).isEmpty();
    list.reportIfAnyItem(e -> true, "message");
    assertNoIssueReported();
  }

  @Test
  void shouldReportAllMatchingItems() {
    ListObject list = ListObject.fromPresent(ctx, sequenceTree, null, null);
    list.reportItemIf(item -> ((ScalarTree) item).value().contains("item"), "message");
    list.items.forEach(item -> assertIssueReported(item, "message"));
  }

  private void assertNoIssueReported() {
    verify(ctx, never()).reportIssue(any(HasTextRange.class), anyString(), anyList());
  }

  private void assertIssueReported(TextRange textRange, String message) {
    verify(ctx).reportIssue(textRange, message);
  }

  private void assertIssueReported(YamlTree tree, String message) {
    verify(ctx).reportIssue(tree, message);
  }
}
