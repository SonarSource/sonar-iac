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
package org.sonar.iac.cloudformation.tree;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.parser.CloudformationParser;
import org.sonar.iac.cloudformation.tree.FunctionCallTree.Style;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

class FunctionCallTreeImplTest {

  @Test
  void shouldParseFunctionCall() {
    assertShortFunctionCall("!GetAtt logicalNameOfResource.attributeName");
    assertShortFunctionCall("!Ref logicalNameOfResource");
    assertFullFunctionCall("Fn::GetAtt: [logicalNameOfResource, attributeName]");
    assertFullFunctionCall("Fn::GetAtt:\n   - logicalNameOfResource\n   - attributeName");
    assertFullFunctionCall("{\"Fn::GetAtt\": [\"logicalNameOfResource\", \"attributeName\"]}");
    assertFullFunctionCall("Ref: logicalNameOfResource");

    assertNoFunctionCall("GetAtt logicalNameOfResource.attributeName");
    assertNoFunctionCall("Fn:GetAtt: [logicalNameOfResource, attributeName]");
    assertNoFunctionCall("{\"Fn:GetAtt\": [\"logicalNameOfResource\", \"attributeName\"]}");
    assertNoFunctionCall("Ref logicalNameOfResource.attributeName");
  }

  @Test
  void shouldGetFunctionCallName() {
    assertThat(parseFunctionCall("!GetAtt logicalNameOfResource.attributeName").name()).isEqualTo("GetAtt");
    assertThat(parseFunctionCall("Fn::GetAtt: [logicalNameOfResource, attributeName]").name()).isEqualTo("GetAtt");
    assertThat(parseFunctionCall("!Ref logicalNameOfResource").name()).isEqualTo("Ref");
    assertThat(parseFunctionCall("Ref: logicalNameOfResource").name()).isEqualTo("Ref");

    assertThat(parseFunctionCall("!UnknownFunction logicalNameOfResource.attributeName").name()).isEqualTo("UnknownFunction");
    assertThat(parseFunctionCall("Fn::UnknownFunction: [logicalNameOfResource, attributeName]").name()).isEqualTo("UnknownFunction");
  }

  @Test
  void shouldGetFunctionCallTextRange() {
    assertThat(parseFunctionCall("!GetAtt logicalNameOfResource.attributeName").textRange())
      .hasRange(1, 0, 1, 43);
    assertThat(parseFunctionCall("Fn::GetAtt: [logicalNameOfResource, attributeName]").textRange())
      .hasRange(1, 0, 1, 50);
    assertThat(parseFunctionCall("Fn::GetAtt:\n   - logicalNameOfResource\n   - attributeName").textRange())
      .hasRange(1, 0, 3, 18);
    assertThat(parseFunctionCall("Fn::GetAtt:").textRange())
      .hasRange(1, 0, 1, 11);
  }

  @Test
  void shouldGetFunctionCallToHighlight() {
    assertThat(parseFunctionCall("!GetAtt logicalNameOfResource.attributeName").toHighlight())
      .hasRange(1, 0, 1, 43);
    assertThat(parseFunctionCall("Fn::GetAtt: [logicalNameOfResource, attributeName]").toHighlight())
      .hasRange(1, 13, 1, 34);
    assertThat(parseFunctionCall("Fn::GetAtt:\n   - logicalNameOfResource\n   - attributeName").toHighlight())
      .hasRange(2, 5, 2, 26);
    assertThat(parseFunctionCall("Fn::GetAtt:").toHighlight())
      .hasRange(1, 11, 1, 11);
  }

  @Test
  void shouldGetFunctionCallArguments() {
    assertThat(parseFunctionCall("!GetAtt logicalNameOfResource.attributeName").arguments()).hasSize(1);
    assertThat(parseFunctionCall("Fn::GetAtt: [logicalNameOfResource, attributeName]").arguments()).hasSize(2);
    assertThat(parseFunctionCall("Fn::GetAtt:\n   - logicalNameOfResource\n   - attributeName").arguments()).hasSize(2);
  }

  @Test
  void shouldParseFunctionCallWithMultipleArguments() {
    FunctionCallTree tree = parse("{'Fn::Sub': ['foo', {'foo':'bar'}]}", FunctionCallTree.class);
    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.arguments().get(0)).isInstanceOf(ScalarTree.class);
    assertThat(tree.arguments().get(1)).isInstanceOf(MappingTree.class);
  }

  @Test
  void shouldParseFunctionCallWithVariable() {
    FunctionCallTree tree = parse("!Sub 'arn:aws:iam::${AWS::AccountId}:root'", FunctionCallTree.class);
    assertThat(tree.arguments()).hasSize(1);
    assertThat(tree.arguments().get(0)).isInstanceOfSatisfying(ScalarTree.class, argument -> {
      assertThat(argument.value()).isEqualTo("arn:aws:iam::${AWS::AccountId}:root");
    });
  }

  @Test
  void shouldParseNestedFunctionCall() {
    FunctionCallTree tree = parse("!Join ['/', ['/aws/lambda', !Ref MyLambdaFunction]]", FunctionCallTree.class);
    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.arguments().get(1)).isInstanceOfSatisfying(SequenceTree.class, sequence -> {
      assertThat(sequence.elements()).hasSize(2);
      assertThat(sequence.elements().get(1)).isInstanceOfSatisfying(FunctionCallTree.class, nestedFunction -> {
        assertThat(nestedFunction.name()).isEqualTo("Ref");
        assertThat(nestedFunction.arguments()).hasSize(1);
      });
    });
  }

  private void assertFullFunctionCall(String source) {
    assertThat(parseFunctionCall(source).style()).isEqualTo(Style.FULL);
  }

  private void assertShortFunctionCall(String source) {
    assertThat(parseFunctionCall(source).style()).isEqualTo(Style.SHORT);
  }

  private FunctionCallTree parseFunctionCall(String source) {
    return parse(source, FunctionCallTree.class);
  }

  private void assertNoFunctionCall(String source) {
    Assertions.assertThat(parse(source, YamlTree.class)).isNotInstanceOf(FunctionCallTree.class);
  }

  protected static FileTree parse(String source) {
    CloudformationParser parser = new CloudformationParser();
    return parser.parse(source, null);
  }

  protected static <T extends YamlTree> T parse(String source, Class<T> clazz) {
    FileTree fileTree = parse(source);
    assertThat(fileTree.documents()).as("Parsed source code contains not a single document").hasSize(1);
    YamlTree rootTree = fileTree.documents().get(0);
    assertThat(rootTree).isInstanceOf(clazz);
    return (T) rootTree;
  }
}
