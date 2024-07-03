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
package org.sonar.iac.helm.tree;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.iac.helm.tree.api.CommandNode;
import org.sonar.iac.helm.tree.api.ListNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.NodeType;
import org.sonar.iac.helm.tree.api.PipeNode;
import org.sonar.iac.helm.tree.api.VariableNode;
import org.sonar.iac.helm.tree.impl.ActionNodeImpl;
import org.sonar.iac.helm.tree.impl.BoolNodeImpl;
import org.sonar.iac.helm.tree.impl.BreakNodeImpl;
import org.sonar.iac.helm.tree.impl.ChainNodeImpl;
import org.sonar.iac.helm.tree.impl.CommandNodeImpl;
import org.sonar.iac.helm.tree.impl.CommentNodeImpl;
import org.sonar.iac.helm.tree.impl.ContinueNodeImpl;
import org.sonar.iac.helm.tree.impl.DotNodeImpl;
import org.sonar.iac.helm.tree.impl.FieldNodeImpl;
import org.sonar.iac.helm.tree.impl.IdentifierNodeImpl;
import org.sonar.iac.helm.tree.impl.IfNodeImpl;
import org.sonar.iac.helm.tree.impl.ListNodeImpl;
import org.sonar.iac.helm.tree.impl.NilNodeImpl;
import org.sonar.iac.helm.tree.impl.NumberNodeImpl;
import org.sonar.iac.helm.tree.impl.PipeNodeImpl;
import org.sonar.iac.helm.tree.impl.RangeNodeImpl;
import org.sonar.iac.helm.tree.impl.StringNodeImpl;
import org.sonar.iac.helm.tree.impl.TemplateNodeImpl;
import org.sonar.iac.helm.tree.impl.TextNodeImpl;
import org.sonar.iac.helm.tree.impl.VariableNodeImpl;
import org.sonar.iac.helm.tree.impl.WithNodeImpl;
import org.sonar.iac.kubernetes.KubernetesAssertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class NodesTest {
  @Test
  void shouldBuildActionNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var actionNode = new ActionNodeImpl(range(1, 0, 1, 5), pipeNode);

    KubernetesAssertions.assertThat(actionNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(actionNode.type()).isEqualTo(NodeType.NODE_ACTION);
    assertThat(actionNode.pipe()).isEqualTo(pipeNode);
  }

  @Test
  void shouldBuildBoolNode() {
    var boolNode = new BoolNodeImpl(range(1, 0, 1, 5), true);

    KubernetesAssertions.assertThat(boolNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(boolNode.type()).isEqualTo(NodeType.NODE_BOOL);
    assertThat(boolNode.value()).isTrue();
  }

  @Test
  void shouldBuildBreakNode() {
    var breakNode = new BreakNodeImpl(range(1, 0, 1, 5), 5);

    KubernetesAssertions.assertThat(breakNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(breakNode.type()).isEqualTo(NodeType.NODE_BREAK);
    assertThat(breakNode.line()).isEqualTo(5);
  }

  @Test
  void shouldBuildChainNode() {
    var field = (List<String>) Mockito.mock(List.class);
    var node = Mockito.mock(Node.class);
    var chainNode = new ChainNodeImpl(range(1, 0, 1, 5), node, field);

    KubernetesAssertions.assertThat(chainNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(chainNode.type()).isEqualTo(NodeType.NODE_CHAIN);
    assertThat(chainNode.fields()).isEqualTo(field);
    assertThat(chainNode.node()).hasValue(node);
  }

  @Test
  void shouldBuildCommandNode() {
    var arguments = (List<Node>) Mockito.mock(List.class);
    var commandNode = new CommandNodeImpl(range(1, 0, 1, 5), arguments);

    KubernetesAssertions.assertThat(commandNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(commandNode.type()).isEqualTo(NodeType.NODE_COMMAND);
    assertThat(commandNode.arguments()).isEqualTo(arguments);
  }

  @Test
  void shouldBuildCommentNode() {
    var commentNode = new CommentNodeImpl(range(1, 0, 1, 5), "/* foo */");

    KubernetesAssertions.assertThat(commentNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(commentNode.type()).isEqualTo(NodeType.NODE_COMMENT);
    assertThat(commentNode.contentText()).isEqualTo("foo");
    assertThat(commentNode.value()).isEqualTo("/* foo */");
  }

  @Test
  void shouldBuildContinueNode() {
    var continueNode = new ContinueNodeImpl(range(1, 0, 1, 5), 5);

    KubernetesAssertions.assertThat(continueNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(continueNode.type()).isEqualTo(NodeType.NODE_CONTINUE);
    assertThat(continueNode.line()).isEqualTo(5);
  }

  @Test
  void shouldBuildDotNode() {
    var dotNode = new DotNodeImpl(range(1, 0, 1, 5));

    KubernetesAssertions.assertThat(dotNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(dotNode.type()).isEqualTo(NodeType.NODE_DOT);
  }

  @Test
  void shouldBuildFieldNode() {
    var identifiers = (List<String>) Mockito.mock(List.class);
    var fieldNode = new FieldNodeImpl(range(1, 0, 1, 5), identifiers);

    KubernetesAssertions.assertThat(fieldNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(fieldNode.type()).isEqualTo(NodeType.NODE_FIELD);
    assertThat(fieldNode.identifiers()).isEqualTo(identifiers);
  }

  @Test
  void shouldBuildIdentifierNode() {
    var identifierNode = new IdentifierNodeImpl(range(1, 0, 1, 5), "name");

    KubernetesAssertions.assertThat(identifierNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(identifierNode.type()).isEqualTo(NodeType.NODE_IDENTIFIER);
    assertThat(identifierNode.identifier()).isEqualTo("name");
  }

  @Test
  void shouldBuildIfNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var list = Mockito.mock(ListNode.class);
    var elseList = Mockito.mock(ListNode.class);
    var ifNode = new IfNodeImpl(range(1, 0, 1, 5), pipeNode, list, elseList);

    KubernetesAssertions.assertThat(ifNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(ifNode.type()).isEqualTo(NodeType.NODE_IF);
    assertThat(ifNode.pipe()).isEqualTo(pipeNode);
    assertThat(ifNode.list()).isEqualTo(list);
    assertThat(ifNode.elseList()).isEqualTo(elseList);
  }

  @Test
  void shouldBuildListNode() {
    var nodes = (List<Node>) Mockito.mock(List.class);
    var listNode = new ListNodeImpl(range(1, 0, 1, 5), nodes);

    KubernetesAssertions.assertThat(listNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(listNode.type()).isEqualTo(NodeType.NODE_LIST);
    assertThat(listNode.nodes()).isEqualTo(nodes);
  }

  @Test
  void shouldBuildNilNode() {
    var nilNode = new NilNodeImpl(range(1, 0, 1, 5));

    KubernetesAssertions.assertThat(nilNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(nilNode.type()).isEqualTo(NodeType.NODE_NIL);
  }

  @Test
  void shouldBuildNumberNode() {
    var numberNode = new NumberNodeImpl(range(1, 0, 1, 5), "5");

    KubernetesAssertions.assertThat(numberNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(numberNode.type()).isEqualTo(NodeType.NODE_NUMBER);
    assertThat(numberNode.text()).isEqualTo("5");
  }

  @Test
  void shouldBuildPipeNode() {
    var commands = (List<CommandNode>) Mockito.mock(List.class);
    var declarations = (List<VariableNode>) Mockito.mock(List.class);
    var pipeNode = new PipeNodeImpl(range(1, 0, 1, 5), declarations, commands);

    KubernetesAssertions.assertThat(pipeNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(pipeNode.type()).isEqualTo(NodeType.NODE_PIPE);
    assertThat(pipeNode.declarations()).isEqualTo(declarations);
    assertThat(pipeNode.commands()).isEqualTo(commands);
  }

  @Test
  void shouldBuildRangeNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var list = Mockito.mock(ListNode.class);
    var elseList = Mockito.mock(ListNode.class);
    var rangeNode = new RangeNodeImpl(range(1, 0, 1, 5), pipeNode, list, elseList);

    KubernetesAssertions.assertThat(rangeNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(rangeNode.type()).isEqualTo(NodeType.NODE_RANGE);
    assertThat(rangeNode.pipe()).isEqualTo(pipeNode);
    assertThat(rangeNode.list()).isEqualTo(list);
    assertThat(rangeNode.elseList()).isEqualTo(elseList);
  }

  @Test
  void shouldBuildStringNode() {
    var stringNode = new StringNodeImpl(range(1, 0, 1, 5), "name");

    KubernetesAssertions.assertThat(stringNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(stringNode.type()).isEqualTo(NodeType.NODE_STRING);
    assertThat(stringNode.text()).isEqualTo("name");
  }

  @Test
  void shouldBuildTemplateNode() {
    var pipe = Mockito.mock(PipeNode.class);
    var templateNode = new TemplateNodeImpl(range(1, 0, 1, 5), "name", pipe);

    KubernetesAssertions.assertThat(templateNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(templateNode.type()).isEqualTo(NodeType.NODE_TEMPLATE);
    assertThat(templateNode.name()).isEqualTo("name");
    assertThat(templateNode.pipe()).isEqualTo(pipe);
  }

  @Test
  void shouldBuildTextNode() {
    var textNode = new TextNodeImpl(range(1, 0, 1, 5), "name");

    KubernetesAssertions.assertThat(textNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(textNode.type()).isEqualTo(NodeType.NODE_TEXT);
    assertThat(textNode.text()).isEqualTo("name");
  }

  @Test
  void shouldBuildVariableNode() {
    var variableNode = new VariableNodeImpl(range(1, 0, 1, 5), List.of("name"));

    KubernetesAssertions.assertThat(variableNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(variableNode.type()).isEqualTo(NodeType.NODE_VARIABLE);
    assertThat(variableNode.idents()).containsExactly("name");
  }

  @Test
  void shouldBuildWithNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var list = Mockito.mock(ListNode.class);
    var elseList = Mockito.mock(ListNode.class);
    var withNode = new WithNodeImpl(range(1, 0, 1, 5), pipeNode, list, elseList);

    KubernetesAssertions.assertThat(withNode.textRange()).hasRange(1, 0, 1, 5);
    assertThat(withNode.type()).isEqualTo(NodeType.NODE_WITH);
    assertThat(withNode.pipe()).isEqualTo(pipeNode);
    assertThat(withNode.list()).isEqualTo(list);
    assertThat(withNode.elseList()).isEqualTo(elseList);
  }
}
