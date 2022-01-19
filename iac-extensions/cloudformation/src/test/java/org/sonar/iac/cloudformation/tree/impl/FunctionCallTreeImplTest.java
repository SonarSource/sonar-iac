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
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;

class FunctionCallTreeImplTest extends CloudformationTreeTest {

  @Test
  void short_style_function_call_with_single_argument() {
    FunctionCallTree tree = (FunctionCallTree) parse("!GetAtt logicalNameOfResource.attributeName").root();
    assertThat(tree.tag()).isEqualTo("FUNCTION_CALL");
    assertThat(tree.name()).isEqualTo("GetAtt");
    assertThat(tree.style()).isEqualTo(FunctionCallTree.Style.SHORT);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 43);

    assertThat(tree.arguments()).hasSize(1);
    assertThat(tree.arguments().get(0)).isInstanceOfSatisfying(ScalarTree.class, argument -> {
      assertThat(argument.style()).isEqualTo(ScalarTree.Style.OTHER);
      assertThat(argument.value()).isEqualTo("logicalNameOfResource.attributeName");
      assertThat(argument.tag()).isEqualTo("tag:yaml.org,2002:str");
      assertTextRange(argument.textRange()).hasRange(1, 0, 1, 43);
    });
  }

  @Test
  void short_style_function_call_with_multiple_arguments() {
    FunctionCallTree tree = (FunctionCallTree) parse("!FindInMap [ MapName, TopLevelKey, SecondLevelKey ]").root();
    assertThat(tree.tag()).isEqualTo("FUNCTION_CALL");
    assertThat(tree.name()).isEqualTo("FindInMap");
    assertThat(tree.style()).isEqualTo(FunctionCallTree.Style.SHORT);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 51);

    assertThat(tree.arguments()).hasSize(3);
    assertThat(tree.arguments().get(0)).isInstanceOfSatisfying(ScalarTree.class, argument -> {
      assertThat(argument.style()).isEqualTo(ScalarTree.Style.PLAIN);
      assertThat(argument.value()).isEqualTo("MapName");
      assertThat(argument.tag()).isEqualTo("tag:yaml.org,2002:str");
      assertTextRange(argument.textRange()).hasRange(1, 13, 1, 20);
    });
  }

  @Test
  void full_style_function_call_with_multiple_arguments() {
    FunctionCallTree tree = (FunctionCallTree) parse("Fn::GetAtt: [ logicalNameOfResource, attributeName ]").root();
    assertThat(tree.tag()).isEqualTo("FUNCTION_CALL");
    assertThat(tree.name()).isEqualTo("GetAtt");
    assertThat(tree.style()).isEqualTo(FunctionCallTree.Style.FULL);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 52);

    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.arguments().get(0)).isInstanceOfSatisfying(ScalarTree.class, argument -> {
      assertThat(argument.style()).isEqualTo(ScalarTree.Style.PLAIN);
      assertThat(argument.value()).isEqualTo("logicalNameOfResource");
      assertThat(argument.tag()).isEqualTo("tag:yaml.org,2002:str");
      assertTextRange(argument.textRange()).hasRange(1, 14, 1, 35);
    });
  }

  @Test
  void json_function_call_with_single_argument() {
    FunctionCallTree tree = (FunctionCallTree) parse("{ \"Fn::GetAZs\" : \"region\" }").root();
    assertThat(tree.tag()).isEqualTo("FUNCTION_CALL");
    assertThat(tree.name()).isEqualTo("GetAZs");
    assertThat(tree.style()).isEqualTo(FunctionCallTree.Style.FULL);
    assertTextRange(tree.textRange()).hasRange(1, 2, 1, 25);

    assertThat(tree.arguments()).hasSize(1);
    assertThat(tree.arguments().get(0)).isInstanceOfSatisfying(ScalarTree.class, argument -> {
      assertThat(argument.style()).isEqualTo(ScalarTree.Style.DOUBLE_QUOTED);
      assertThat(argument.value()).isEqualTo("region");
      assertThat(argument.tag()).isEqualTo("tag:yaml.org,2002:str");
      assertTextRange(argument.textRange()).hasRange(1, 17, 1, 25);
    });
  }

  @Test
  void json_function_call_with_multiple_arguments() {
    FunctionCallTree tree = (FunctionCallTree) parse("{'Fn::Sub': ['foo', {'foo':'bar'}]}").root();
    assertThat(tree.tag()).isEqualTo("FUNCTION_CALL");
    assertThat(tree.name()).isEqualTo("Sub");
    assertThat(tree.style()).isEqualTo(FunctionCallTree.Style.FULL);
    assertTextRange(tree.textRange()).hasRange(1, 1, 1, 34);

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


}
