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
import org.assertj.core.api.Assertions;
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

import static org.assertj.core.api.Assertions.*;

class NodesTest {
  @Test
  void shouldBuildActionNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var actionNode = new ActionNodeImpl(1, 5, pipeNode);

    KubernetesAssertions.assertThat(actionNode.location()).hasLocation(1, 5);
    assertThat(actionNode.type()).isEqualTo(NodeType.NODE_ACTION);
    assertThat(actionNode.pipe()).isEqualTo(pipeNode);
  }

  @Test
  void shouldBuildBoolNode() {
    var boolNode = new BoolNodeImpl(1, 5, true);

    KubernetesAssertions.assertThat(boolNode.location()).hasLocation(1, 5);
    assertThat(boolNode.type()).isEqualTo(NodeType.NODE_BOOL);
    assertThat(boolNode.value()).isTrue();
  }

  @Test
  void shouldBuildBreakNode() {
    var breakNode = new BreakNodeImpl(1, 5, 5);

    KubernetesAssertions.assertThat(breakNode.location()).hasLocation(1, 5);
    assertThat(breakNode.type()).isEqualTo(NodeType.NODE_BREAK);
    assertThat(breakNode.line()).isEqualTo(5);
  }

  @Test
  void shouldBuildChainNode() {
    var field = (List<String>) Mockito.mock(List.class);
    var node = Mockito.mock(Node.class);
    var chainNode = new ChainNodeImpl(1, 5, node, field);

    KubernetesAssertions.assertThat(chainNode.location()).hasLocation(1, 5);
    assertThat(chainNode.type()).isEqualTo(NodeType.NODE_CHAIN);
    assertThat(chainNode.fields()).isEqualTo(field);
    assertThat(chainNode.node()).hasValue(node);
  }

  @Test
  void shouldBuildCommandNode() {
    var arguments = (List<Node>) Mockito.mock(List.class);
    var commandNode = new CommandNodeImpl(1, 5, arguments);

    KubernetesAssertions.assertThat(commandNode.location()).hasLocation(1, 5);
    assertThat(commandNode.type()).isEqualTo(NodeType.NODE_COMMAND);
    assertThat(commandNode.arguments()).isEqualTo(arguments);
  }

  @Test
  void shouldBuildCommentNode() {
    var commentNode = new CommentNodeImpl(1, 5, "/* foo */");

    KubernetesAssertions.assertThat(commentNode.location()).hasLocation(1, 5);
    assertThat(commentNode.type()).isEqualTo(NodeType.NODE_COMMENT);
    assertThat(commentNode.text()).isEqualTo("/* foo */");
  }

  @Test
  void shouldBuildContinueNode() {
    var continueNode = new ContinueNodeImpl(1, 5, 5);

    KubernetesAssertions.assertThat(continueNode.location()).hasLocation(1, 5);
    assertThat(continueNode.type()).isEqualTo(NodeType.NODE_CONTINUE);
    assertThat(continueNode.line()).isEqualTo(5);
  }

  @Test
  void shouldBuildDotNode() {
    var dotNode = new DotNodeImpl(1, 5);

    KubernetesAssertions.assertThat(dotNode.location()).hasLocation(1, 5);
    assertThat(dotNode.type()).isEqualTo(NodeType.NODE_DOT);
  }

  @Test
  void shouldBuildFieldNode() {
    var identifiers = (List<String>) Mockito.mock(List.class);
    var fieldNode = new FieldNodeImpl(1, 5, identifiers);

    KubernetesAssertions.assertThat(fieldNode.location()).hasLocation(1, 5);
    assertThat(fieldNode.type()).isEqualTo(NodeType.NODE_FIELD);
    assertThat(fieldNode.identifiers()).isEqualTo(identifiers);
  }

  @Test
  void shouldBuildIdentifierNode() {
    var identifierNode = new IdentifierNodeImpl(1, 5, "name");

    KubernetesAssertions.assertThat(identifierNode.location()).hasLocation(1, 5);
    assertThat(identifierNode.type()).isEqualTo(NodeType.NODE_IDENTIFIER);
    assertThat(identifierNode.identifier()).isEqualTo("name");
  }

  @Test
  void shouldBuildIfNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var list = Mockito.mock(ListNode.class);
    var elseList = Mockito.mock(ListNode.class);
    var ifNode = new IfNodeImpl(1, 5, pipeNode, list, elseList);

    KubernetesAssertions.assertThat(ifNode.location()).hasLocation(1, 5);
    assertThat(ifNode.type()).isEqualTo(NodeType.NODE_IF);
    assertThat(ifNode.pipe()).isEqualTo(pipeNode);
    assertThat(ifNode.list()).isEqualTo(list);
    assertThat(ifNode.elseList()).isEqualTo(elseList);
  }

  @Test
  void shouldBuildListNode() {
    var nodes = (List<Node>) Mockito.mock(List.class);
    var listNode = new ListNodeImpl(1, 5, nodes);

    KubernetesAssertions.assertThat(listNode.location()).hasLocation(1, 5);
    assertThat(listNode.type()).isEqualTo(NodeType.NODE_LIST);
    assertThat(listNode.nodes()).isEqualTo(nodes);
  }

  @Test
  void shouldBuildNilNode() {
    var nilNode = new NilNodeImpl(1, 5);

    KubernetesAssertions.assertThat(nilNode.location()).hasLocation(1, 5);
    assertThat(nilNode.type()).isEqualTo(NodeType.NODE_NIL);
  }

  @Test
  void shouldBuildNumberNode() {
    var numberNode = new NumberNodeImpl(1, 5, "5");

    KubernetesAssertions.assertThat(numberNode.location()).hasLocation(1, 5);
    assertThat(numberNode.type()).isEqualTo(NodeType.NODE_NUMBER);
    assertThat(numberNode.text()).isEqualTo("5");
  }

  @Test
  void shouldBuildPipeNode() {
    var commands = (List<CommandNode>) Mockito.mock(List.class);
    var declarations = (List<VariableNode>) Mockito.mock(List.class);
    var pipeNode = new PipeNodeImpl(1, 5, declarations, commands);

    KubernetesAssertions.assertThat(pipeNode.location()).hasLocation(1, 5);
    assertThat(pipeNode.type()).isEqualTo(NodeType.NODE_PIPE);
    assertThat(pipeNode.declarations()).isEqualTo(declarations);
    assertThat(pipeNode.commands()).isEqualTo(commands);
  }

  @Test
  void shouldBuildRangeNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var list = Mockito.mock(ListNode.class);
    var elseList = Mockito.mock(ListNode.class);
    var rangeNode = new RangeNodeImpl(1, 5, pipeNode, list, elseList);

    KubernetesAssertions.assertThat(rangeNode.location()).hasLocation(1, 5);
    assertThat(rangeNode.type()).isEqualTo(NodeType.NODE_RANGE);
    assertThat(rangeNode.pipe()).isEqualTo(pipeNode);
    assertThat(rangeNode.list()).isEqualTo(list);
    assertThat(rangeNode.elseList()).isEqualTo(elseList);
  }

  @Test
  void shouldBuildStringNode() {
    var stringNode = new StringNodeImpl(1, 5, "name");

    KubernetesAssertions.assertThat(stringNode.location()).hasLocation(1, 5);
    assertThat(stringNode.type()).isEqualTo(NodeType.NODE_STRING);
    assertThat(stringNode.text()).isEqualTo("name");
  }

  @Test
  void shouldBuildTemplateNode() {
    var pipe = Mockito.mock(PipeNode.class);
    var templateNode = new TemplateNodeImpl(1, 5, "name", pipe);

    KubernetesAssertions.assertThat(templateNode.location()).hasLocation(1, 5);
    assertThat(templateNode.type()).isEqualTo(NodeType.NODE_TEMPLATE);
    assertThat(templateNode.name()).isEqualTo("name");
    assertThat(templateNode.pipe()).isEqualTo(pipe);
  }

  @Test
  void shouldBuildTextNode() {
    var textNode = new TextNodeImpl(1, 5, "name");

    KubernetesAssertions.assertThat(textNode.location()).hasLocation(1, 5);
    assertThat(textNode.type()).isEqualTo(NodeType.NODE_TEXT);
    assertThat(textNode.text()).isEqualTo("name");
  }

  @Test
  void shouldBuildVariableNode() {
    var variableNode = new VariableNodeImpl(1, 5, List.of("name"));

    KubernetesAssertions.assertThat(variableNode.location()).hasLocation(1, 5);
    assertThat(variableNode.type()).isEqualTo(NodeType.NODE_VARIABLE);
    assertThat(variableNode.idents()).containsExactly("name");
  }

  @Test
  void shouldBuildWithNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var list = Mockito.mock(ListNode.class);
    var elseList = Mockito.mock(ListNode.class);
    var withNode = new WithNodeImpl(1, 5, pipeNode, list, elseList);

    KubernetesAssertions.assertThat(withNode.location()).hasLocation(1, 5);
    assertThat(withNode.type()).isEqualTo(NodeType.NODE_WITH);
    assertThat(withNode.pipe()).isEqualTo(pipeNode);
    assertThat(withNode.list()).isEqualTo(list);
    assertThat(withNode.elseList()).isEqualTo(elseList);
  }
}
