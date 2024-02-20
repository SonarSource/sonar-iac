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
package org.sonar.iac.helm.tree.utils;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.tree.api.CommandNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.PipeNode;
import org.sonar.iac.helm.tree.impl.ActionNodeImpl;
import org.sonar.iac.helm.tree.impl.CommandNodeImpl;
import org.sonar.iac.helm.tree.impl.FieldNodeImpl;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;
import org.sonar.iac.helm.tree.impl.IdentifierNodeImpl;
import org.sonar.iac.helm.tree.impl.ListNodeImpl;
import org.sonar.iac.helm.tree.impl.NumberNodeImpl;
import org.sonar.iac.helm.tree.impl.PipeNodeImpl;
import org.sonar.iac.helm.tree.impl.TextNodeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class GoTemplateAstHelperTest {

  public static List<Arguments> textRanges() {
    return List.of(
      of(range(2, 0, 2, 36), "bigger than node"),
      of(range(2, 5, 2, 22), "shifted left from node"),
      of(range(2, 15, 2, 35), "shifted right from node"),
      of(range(2, 23, 2, 24), "smaller than node"));
  }

  @ParameterizedTest(name = "should find values by TextRange {1}")
  @MethodSource("textRanges")
  void shouldFindValuesByTextRange(TextRange textRange, String name) {
    String sourceCode = code("apiVersion: apps/v1 #1",
      "hostIPC: {{ .Values.hostIPC }} #2 #3");
    var textNode1 = new TextNodeImpl(0, 32, "apiVersion: apps/v1 #1\nhostIPC: ");
    var textNode2 = new TextNodeImpl(53, 7, " #2\n #3");
    var fieldNode = new FieldNodeImpl(42, 15, List.of("Values", "hostIPC"));
    var command = new CommandNodeImpl(35, 15, List.of(fieldNode));
    var pipeNode = new PipeNodeImpl(35, 15, List.of(), List.of(command));
    var actionNode = new ActionNodeImpl(35, 19, pipeNode);
    ListNodeImpl root = new ListNodeImpl(0, 57, List.of(textNode1, actionNode, textNode2));
    var goTemplateTree = new GoTemplateTreeImpl("name", "name", 0, root);

    var actual = GoTemplateAstHelper.findNodes(
      goTemplateTree,
      textRange,
      sourceCode);

    assertThat(actual).contains(new ValuePath("Values", "hostIPC"));
  }

  @Test
  void shouldFindValuesInToYaml() {
    String sourceCode = "{{ toYaml (default .Values.initContainers.securityContext .Values.initSysctl.securityContext) | indent 12 }} #1\n #2";
    Node identifier = new IdentifierNodeImpl(3, 6, "toYaml");
    Node defaultFunction = new IdentifierNodeImpl(11, 7, "default");
    Node initContainersValue = new FieldNodeImpl(26, 3, List.of("Values", "initContainers", "securityContext"));
    Node initSysctlValue = new FieldNodeImpl(65, 3, List.of("Values", "initSysctl", "securityContext"));
    CommandNode command3 = new CommandNodeImpl(11, 81, List.of(defaultFunction, initContainersValue, initSysctlValue));
    Node pipeNode1 = new PipeNodeImpl(11, 81, List.of(), List.of(command3));
    CommandNode command1 = new CommandNodeImpl(3, 90, List.of(identifier, pipeNode1));
    Node indentNode = new IdentifierNodeImpl(96, 6, "indent");
    Node numberNode = new NumberNodeImpl(103, 2, "12");
    CommandNode command2 = new CommandNodeImpl(96, 9, List.of(indentNode, numberNode));
    PipeNode pipeNode = new PipeNodeImpl(3, 102, List.of(), List.of(command1, command2));
    Node actionNode = new ActionNodeImpl(3, 106, pipeNode);
    Node textNode = new TextNodeImpl(108, 7, " #1\n #2");
    ListNodeImpl root = new ListNodeImpl(0, 113, List.of(actionNode, textNode));
    var goTemplateTree = new GoTemplateTreeImpl("name", "name", 0, root);

    var actual = GoTemplateAstHelper.findNodes(
      goTemplateTree,
      range(1, 0, 1, 108),
      sourceCode);

    assertThat(actual).contains(
      new ValuePath("Values", "initContainers", "securityContext"),
      new ValuePath("Values", "initSysctl", "securityContext"));
  }

  @Test
  void shouldFindValuesForTwoHelmExpressionsInRange() {
    String sourceCode = code("apiVersion: apps/v1 #1",
      "hostIPC: {{ .Values.hostIPC }}{{ .Values.foo.bar }} #2 #3");
    var textNode1 = new TextNodeImpl(0, 32, "apiVersion: apps/v1 #1\nhostIPC: ");
    var textNode2 = new TextNodeImpl(74, 7, " #2\n #3");
    var fieldNode = new FieldNodeImpl(42, 14, List.of("Values", "hostIPC"));
    var command = new CommandNodeImpl(35, 14, List.of(fieldNode));
    var pipeNode = new PipeNodeImpl(35, 14, List.of(), List.of(command));
    var actionNode = new ActionNodeImpl(35, 14, pipeNode);

    var fieldNode2 = new FieldNodeImpl(60, 14, List.of("Values", "foo", "bar"));
    var command2 = new CommandNodeImpl(53, 14, List.of(fieldNode2));
    var pipeNode2 = new PipeNodeImpl(53, 14, List.of(), List.of(command2));
    var actionNode2 = new ActionNodeImpl(53, 14, pipeNode2);
    ListNodeImpl root = new ListNodeImpl(0, 14, List.of(textNode1, actionNode, actionNode2, textNode2));
    var goTemplateTree = new GoTemplateTreeImpl("name", "name", 0, root);

    var actual = GoTemplateAstHelper.findNodes(
      goTemplateTree,
      range(2, 2, 2, 45),
      sourceCode);

    assertThat(actual).contains(new ValuePath("Values", "hostIPC"), new ValuePath("Values", "foo", "bar"));
  }
}
