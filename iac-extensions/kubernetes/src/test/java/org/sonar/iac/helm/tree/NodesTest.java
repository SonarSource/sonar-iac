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

class NodesTest {
  @Test
  void shouldBuildActionNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var actionNode = new ActionNodeImpl(1, 5, pipeNode);

    Assertions.assertThat(actionNode.position()).isEqualTo(1);
    Assertions.assertThat(actionNode.length()).isEqualTo(5);
    Assertions.assertThat(actionNode.type()).isEqualTo(NodeType.NODE_ACTION);
    Assertions.assertThat(actionNode.pipe()).isEqualTo(pipeNode);
  }

  @Test
  void shouldBuildBoolNode() {
    var boolNode = new BoolNodeImpl(1, 5, true);

    Assertions.assertThat(boolNode.position()).isEqualTo(1);
    Assertions.assertThat(boolNode.length()).isEqualTo(5);
    Assertions.assertThat(boolNode.type()).isEqualTo(NodeType.NODE_BOOL);
    Assertions.assertThat(boolNode.value()).isTrue();
  }

  @Test
  void shouldBuildBreakNode() {
    var breakNode = new BreakNodeImpl(1, 5, 5);

    Assertions.assertThat(breakNode.position()).isEqualTo(1);
    Assertions.assertThat(breakNode.length()).isEqualTo(5);
    Assertions.assertThat(breakNode.type()).isEqualTo(NodeType.NODE_BREAK);
    Assertions.assertThat(breakNode.line()).isEqualTo(5);
  }

  @Test
  void shouldBuildChainNode() {
    var field = (List<String>) Mockito.mock(List.class);
    var node = Mockito.mock(Node.class);
    var chainNode = new ChainNodeImpl(1, 5, node, field);

    Assertions.assertThat(chainNode.position()).isEqualTo(1);
    Assertions.assertThat(chainNode.length()).isEqualTo(5);
    Assertions.assertThat(chainNode.type()).isEqualTo(NodeType.NODE_CHAIN);
    Assertions.assertThat(chainNode.fields()).isEqualTo(field);
    Assertions.assertThat(chainNode.node()).hasValue(node);
  }

  @Test
  void shouldBuildCommandNode() {
    var arguments = (List<Node>) Mockito.mock(List.class);
    var commandNode = new CommandNodeImpl(1, 5, arguments);

    Assertions.assertThat(commandNode.position()).isEqualTo(1);
    Assertions.assertThat(commandNode.length()).isEqualTo(5);
    Assertions.assertThat(commandNode.type()).isEqualTo(NodeType.NODE_COMMAND);
    Assertions.assertThat(commandNode.arguments()).isEqualTo(arguments);
  }

  @Test
  void shouldBuildContinueNode() {
    var continueNode = new ContinueNodeImpl(1, 5, 5);

    Assertions.assertThat(continueNode.position()).isEqualTo(1);
    Assertions.assertThat(continueNode.length()).isEqualTo(5);
    Assertions.assertThat(continueNode.type()).isEqualTo(NodeType.NODE_CONTINUE);
    Assertions.assertThat(continueNode.line()).isEqualTo(5);
  }

  @Test
  void shouldBuildDotNode() {
    var dotNode = new DotNodeImpl(1, 5);

    Assertions.assertThat(dotNode.position()).isEqualTo(1);
    Assertions.assertThat(dotNode.length()).isEqualTo(5);
    Assertions.assertThat(dotNode.type()).isEqualTo(NodeType.NODE_DOT);
  }

  @Test
  void shouldBuildFieldNode() {
    var identifiers = (List<String>) Mockito.mock(List.class);
    var fieldNode = new FieldNodeImpl(1, 5, identifiers);

    Assertions.assertThat(fieldNode.position()).isEqualTo(1);
    Assertions.assertThat(fieldNode.length()).isEqualTo(5);
    Assertions.assertThat(fieldNode.type()).isEqualTo(NodeType.NODE_FIELD);
    Assertions.assertThat(fieldNode.identifiers()).isEqualTo(identifiers);
  }

  @Test
  void shouldBuildIdentifierNode() {
    var identifierNode = new IdentifierNodeImpl(1, 5, "name");

    Assertions.assertThat(identifierNode.position()).isEqualTo(1);
    Assertions.assertThat(identifierNode.length()).isEqualTo(5);
    Assertions.assertThat(identifierNode.type()).isEqualTo(NodeType.NODE_IDENTIFIER);
    Assertions.assertThat(identifierNode.identifier()).isEqualTo("name");
  }

  @Test
  void shouldBuildIfNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var list = Mockito.mock(ListNode.class);
    var elseList = Mockito.mock(ListNode.class);
    var ifNode = new IfNodeImpl(1, 5, pipeNode, list, elseList);

    Assertions.assertThat(ifNode.position()).isEqualTo(1);
    Assertions.assertThat(ifNode.length()).isEqualTo(5);
    Assertions.assertThat(ifNode.type()).isEqualTo(NodeType.NODE_IF);
    Assertions.assertThat(ifNode.pipe()).isEqualTo(pipeNode);
    Assertions.assertThat(ifNode.list()).isEqualTo(list);
    Assertions.assertThat(ifNode.elseList()).isEqualTo(elseList);
  }

  @Test
  void shouldBuildListNode() {
    var nodes = (List<Node>) Mockito.mock(List.class);
    var listNode = new ListNodeImpl(1, 5, nodes);

    Assertions.assertThat(listNode.position()).isEqualTo(1);
    Assertions.assertThat(listNode.length()).isEqualTo(5);
    Assertions.assertThat(listNode.type()).isEqualTo(NodeType.NODE_LIST);
    Assertions.assertThat(listNode.nodes()).isEqualTo(nodes);
  }

  @Test
  void shouldBuildNilNode() {
    var nilNode = new NilNodeImpl(1, 5);

    Assertions.assertThat(nilNode.position()).isEqualTo(1);
    Assertions.assertThat(nilNode.length()).isEqualTo(5);
    Assertions.assertThat(nilNode.type()).isEqualTo(NodeType.NODE_NIL);
  }

  @Test
  void shouldBuildNumberNode() {
    var numberNode = new NumberNodeImpl(1, 5, "5");

    Assertions.assertThat(numberNode.position()).isEqualTo(1);
    Assertions.assertThat(numberNode.length()).isEqualTo(5);
    Assertions.assertThat(numberNode.type()).isEqualTo(NodeType.NODE_NUMBER);
    Assertions.assertThat(numberNode.text()).isEqualTo("5");
  }

  @Test
  void shouldBuildPipeNode() {
    var commands = (List<CommandNode>) Mockito.mock(List.class);
    var declarations = (List<VariableNode>) Mockito.mock(List.class);
    var pipeNode = new PipeNodeImpl(1, 5, declarations, commands);

    Assertions.assertThat(pipeNode.position()).isEqualTo(1);
    Assertions.assertThat(pipeNode.length()).isEqualTo(5);
    Assertions.assertThat(pipeNode.type()).isEqualTo(NodeType.NODE_PIPE);
    Assertions.assertThat(pipeNode.declarations()).isEqualTo(declarations);
    Assertions.assertThat(pipeNode.commands()).isEqualTo(commands);
  }

  @Test
  void shouldBuildRangeNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var list = Mockito.mock(ListNode.class);
    var elseList = Mockito.mock(ListNode.class);
    var rangeNode = new RangeNodeImpl(1, 5, pipeNode, list, elseList);

    Assertions.assertThat(rangeNode.position()).isEqualTo(1);
    Assertions.assertThat(rangeNode.length()).isEqualTo(5);
    Assertions.assertThat(rangeNode.type()).isEqualTo(NodeType.NODE_RANGE);
    Assertions.assertThat(rangeNode.pipe()).isEqualTo(pipeNode);
    Assertions.assertThat(rangeNode.list()).isEqualTo(list);
    Assertions.assertThat(rangeNode.elseList()).isEqualTo(elseList);
  }

  @Test
  void shouldBuildStringNode() {
    var stringNode = new StringNodeImpl(1, 5, "name");

    Assertions.assertThat(stringNode.position()).isEqualTo(1);
    Assertions.assertThat(stringNode.length()).isEqualTo(5);
    Assertions.assertThat(stringNode.type()).isEqualTo(NodeType.NODE_STRING);
    Assertions.assertThat(stringNode.text()).isEqualTo("name");
  }

  @Test
  void shouldBuildTemplateNode() {
    var pipe = Mockito.mock(PipeNode.class);
    var templateNode = new TemplateNodeImpl(1, 5, "name", pipe);

    Assertions.assertThat(templateNode.position()).isEqualTo(1);
    Assertions.assertThat(templateNode.length()).isEqualTo(5);
    Assertions.assertThat(templateNode.type()).isEqualTo(NodeType.NODE_TEMPLATE);
    Assertions.assertThat(templateNode.name()).isEqualTo("name");
    Assertions.assertThat(templateNode.pipe()).isEqualTo(pipe);
  }

  @Test
  void shouldBuildTextNode() {
    var textNode = new TextNodeImpl(1, 5, "name");

    Assertions.assertThat(textNode.position()).isEqualTo(1);
    Assertions.assertThat(textNode.length()).isEqualTo(5);
    Assertions.assertThat(textNode.type()).isEqualTo(NodeType.NODE_TEXT);
    Assertions.assertThat(textNode.text()).isEqualTo("name");
  }

  @Test
  void shouldBuildVariableNode() {
    var variableNode = new VariableNodeImpl(1, 5, List.of("name"));

    Assertions.assertThat(variableNode.position()).isEqualTo(1);
    Assertions.assertThat(variableNode.length()).isEqualTo(5);
    Assertions.assertThat(variableNode.type()).isEqualTo(NodeType.NODE_VARIABLE);
    Assertions.assertThat(variableNode.idents()).containsExactly("name");
  }

  @Test
  void shouldBuildWithNode() {
    var pipeNode = Mockito.mock(PipeNode.class);
    var list = Mockito.mock(ListNode.class);
    var elseList = Mockito.mock(ListNode.class);
    var withNode = new WithNodeImpl(1, 5, pipeNode, list, elseList);

    Assertions.assertThat(withNode.position()).isEqualTo(1);
    Assertions.assertThat(withNode.length()).isEqualTo(5);
    Assertions.assertThat(withNode.type()).isEqualTo(NodeType.NODE_WITH);
    Assertions.assertThat(withNode.pipe()).isEqualTo(pipeNode);
    Assertions.assertThat(withNode.list()).isEqualTo(list);
    Assertions.assertThat(withNode.elseList()).isEqualTo(elseList);
  }
}
