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
package org.sonar.iac.helm.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.helm.tree.api.ListNode;
import org.sonar.iac.helm.tree.api.PipeNode;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AbstractBranchNodeTest {

  @Test
  void shouldReturnChildrenForWithNode() {
    var pipeNode = mock(PipeNode.class);
    var listNode = mock(ListNode.class);
    var elseListNode = mock(ListNode.class);
    var withNode = new WithNodeImpl(0, 10, pipeNode, listNode, elseListNode);

    var children = withNode.children();

    assertThat(children).containsOnly(pipeNode, listNode, elseListNode);
  }

  @Test
  void shouldReturnPipe() {
    var pipeNode = mock(PipeNode.class);
    var listNode = mock(ListNode.class);
    var elseListNode = mock(ListNode.class);
    var withNode = new WithNodeImpl(0, 10, pipeNode, listNode, elseListNode);

    var actual = withNode.pipe();

    assertThat(actual).isEqualTo(pipeNode);
  }

  @Test
  void shouldReturnList() {
    var pipeNode = mock(PipeNode.class);
    var listNode = mock(ListNode.class);
    var elseListNode = mock(ListNode.class);
    var withNode = new WithNodeImpl(0, 10, pipeNode, listNode, elseListNode);

    var actual = withNode.list();

    assertThat(actual).isEqualTo(listNode);
  }

  @Test
  void shouldReturnElseList() {
    var pipeNode = mock(PipeNode.class);
    var listNode = mock(ListNode.class);
    var elseListNode = mock(ListNode.class);
    var withNode = new WithNodeImpl(0, 10, pipeNode, listNode, elseListNode);

    var actual = withNode.elseList();

    assertThat(actual).isEqualTo(elseListNode);
  }
}
