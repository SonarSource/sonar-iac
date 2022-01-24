/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.cloudformation.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.FunctionCallTree;
import org.sonar.iac.cloudformation.api.tree.FunctionCallTree.Style;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;

class FunctionCallTreeImplTest extends CloudformationTreeTest {

  @Test
  void parse_function_call() {
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
  void get_function_call_name() {
    assertThat(parseFunctionCall("!GetAtt logicalNameOfResource.attributeName").name()).isEqualTo("GetAtt");
    assertThat(parseFunctionCall("Fn::GetAtt: [logicalNameOfResource, attributeName]").name()).isEqualTo("GetAtt");
    assertThat(parseFunctionCall("!Ref logicalNameOfResource").name()).isEqualTo("Ref");
    assertThat(parseFunctionCall("Ref: logicalNameOfResource").name()).isEqualTo("Ref");

    assertThat(parseFunctionCall("!UnknownFunction logicalNameOfResource.attributeName").name()).isEqualTo("UnknownFunction");
    assertThat(parseFunctionCall("Fn::UnknownFunction: [logicalNameOfResource, attributeName]").name()).isEqualTo("UnknownFunction");
  }

  @Test
  void get_function_call_location() {
    assertTextRange(parseFunctionCall("!GetAtt logicalNameOfResource.attributeName").textRange())
      .hasRange(1, 0, 1, 43);
    assertTextRange(parseFunctionCall("Fn::GetAtt: [logicalNameOfResource, attributeName]").textRange())
      .hasRange(1, 0, 1, 50);
    assertTextRange(parseFunctionCall("Fn::GetAtt:\n   - logicalNameOfResource\n   - attributeName").textRange())
      .hasRange(1, 0, 3, 18);
  }

  @Test
  void get_function_call_arguments() {
    assertThat(parseFunctionCall("!GetAtt logicalNameOfResource.attributeName").arguments()).hasSize(1);
    assertThat(parseFunctionCall("Fn::GetAtt: [logicalNameOfResource, attributeName]").arguments()).hasSize(2);
    assertThat(parseFunctionCall("Fn::GetAtt:\n   - logicalNameOfResource\n   - attributeName").arguments()).hasSize(2);
  }

  @Test
  void function_call_with_multiple_arguments() {
    FunctionCallTree tree = (FunctionCallTree) parse("{'Fn::Sub': ['foo', {'foo':'bar'}]}").root();
    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.arguments().get(0)).isInstanceOf(ScalarTree.class);
    assertThat(tree.arguments().get(1)).isInstanceOf(MappingTree.class);
  }

  @Test
  void function_call_with_variable() {
    FunctionCallTree tree = (FunctionCallTree) parse("!Sub 'arn:aws:iam::${AWS::AccountId}:root'").root();
    assertThat(tree.arguments()).hasSize(1);
    assertThat(tree.arguments().get(0)).isInstanceOfSatisfying(ScalarTree.class, argument -> {
      assertThat(argument.value()).isEqualTo("arn:aws:iam::${AWS::AccountId}:root");
    });
  }

  @Test
  void nested_function_call() {
    FunctionCallTree tree = (FunctionCallTree) parse("!Join ['/', ['/aws/lambda', !Ref MyLambdaFunction]]").root();
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
    assertThat(parse(source).root()).isNotInstanceOf(FunctionCallTree.class);
  }
}
