/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.helm.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.helm.tree.api.ListNode;
import org.sonar.iac.helm.tree.api.PipeNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class AbstractBranchNodeTest {

  @Test
  void shouldReturnChildrenForWithNode() {
    var pipeNode = mock(PipeNode.class);
    var listNode = mock(ListNode.class);
    var elseListNode = mock(ListNode.class);
    var withNode = new WithNodeImpl(() -> range(0, 1, 0, 10), pipeNode, listNode, elseListNode);

    var children = withNode.children();

    assertThat(children).containsOnly(pipeNode, listNode, elseListNode);
  }

  @Test
  void shouldReturnPipe() {
    var pipeNode = mock(PipeNode.class);
    var listNode = mock(ListNode.class);
    var elseListNode = mock(ListNode.class);
    var withNode = new WithNodeImpl(() -> range(1, 0, 1, 10), pipeNode, listNode, elseListNode);

    var actual = withNode.pipe();

    assertThat(actual).isEqualTo(pipeNode);
  }

  @Test
  void shouldReturnList() {
    var pipeNode = mock(PipeNode.class);
    var listNode = mock(ListNode.class);
    var elseListNode = mock(ListNode.class);
    var withNode = new WithNodeImpl(() -> range(1, 0, 1, 10), pipeNode, listNode, elseListNode);

    var actual = withNode.list();

    assertThat(actual).isEqualTo(listNode);
  }

  @Test
  void shouldReturnElseList() {
    var pipeNode = mock(PipeNode.class);
    var listNode = mock(ListNode.class);
    var elseListNode = mock(ListNode.class);
    var withNode = new WithNodeImpl(() -> range(1, 0, 1, 10), pipeNode, listNode, elseListNode);

    var actual = withNode.elseList();

    assertThat(actual).isEqualTo(elseListNode);
  }
}
