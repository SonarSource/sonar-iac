/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.common.checks.policy;

import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.CommonTestUtils.TestAttributeTree.attribute;
import static org.sonar.iac.common.checks.CommonTestUtils.TestIterable.list;
import static org.sonar.iac.common.checks.CommonTestUtils.TestPropertiesTree.properties;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTextTree.text;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTree.tree;

class PolicyValidatorTest {

  Tree POSITIVE_TREE = text("value");
  Tree NEGATIVE_TREE = text("not_value");
  Predicate<Tree> TEST_PREDICATE = tree -> tree instanceof TextTree && ((TextTree) tree).value().equals("value");

  @Test
  void exploreTestSingleTree() {
    assertThat(PolicyValidator.explore(TEST_PREDICATE,  POSITIVE_TREE)).isPresent().get().isEqualTo(POSITIVE_TREE);
    assertThat(PolicyValidator.explore(TEST_PREDICATE,  NEGATIVE_TREE)).isEmpty();
    assertThat(PolicyValidator.explore(TEST_PREDICATE, tree())).isEmpty();
  }

  @Test
  void exploreTestListElement() {
    assertThat(PolicyValidator.explore(TEST_PREDICATE, list(POSITIVE_TREE))).isPresent().get().isEqualTo(POSITIVE_TREE);
    assertThat(PolicyValidator.explore(TEST_PREDICATE, list(NEGATIVE_TREE, POSITIVE_TREE))).isPresent().get().isEqualTo(POSITIVE_TREE);
    assertThat(PolicyValidator.explore(TEST_PREDICATE, list(NEGATIVE_TREE))).isEmpty();
  }

  @Test
  void findInsecureResource() {
    assertThat(PolicyValidator.findInsecureResource(text("*"))).isPresent();
    assertThat(PolicyValidator.findInsecureResource(list(text("*")))).isPresent();
    assertThat(PolicyValidator.findInsecureResource(list(text("FooBar"), text("*")))).isPresent();
    assertThat(PolicyValidator.findInsecureResource(text("FooBar"))).isEmpty();
    assertThat(PolicyValidator.findInsecureResource(tree())).isEmpty();
    assertThat(PolicyValidator.findInsecureResource(null)).isEmpty();
  }

  @Test
  void findResourceAccessAction() {
    assertThat(PolicyValidator.findResourceAccessAction(text("sns:Publish"))).isPresent();
    assertThat(PolicyValidator.findResourceAccessAction(text("foo:bar"))).isEmpty();
    assertThat(PolicyValidator.findResourceAccessAction(list(text("foo:bar"), text("sns:Publish")))).isPresent();
  }

  @Test
  void findInsecureStatements() {
    List<Tree> statements = List.of(
      properties(attribute("Action", "sns:Publish"), attribute("Resource", "*"), attribute("Effect", "Allow")),
      properties(attribute("Action", "sns:Publish"), attribute("NotResource", "*"), attribute("Effect", "Deny")),
      properties(attribute("Action", "foo:bar"), attribute("Resource", "*"), attribute("Effect", "Allow")),
      properties(attribute("Action", "sns:Publish"), attribute("Effect", "Allow")),
      properties(attribute("Action", "sns:Publish"), attribute("Resource", "*")),
      properties(attribute("Action", "sns:Publish"), attribute("NotResource", "*"))
    );

    Policy policy = new Policy(null, tree -> statements);
    assertThat(PolicyValidator.findInsecureStatements(policy)).hasSize(2);
  }
}
