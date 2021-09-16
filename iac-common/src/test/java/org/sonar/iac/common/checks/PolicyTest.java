/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.TextUtilsTest.TestTextTree.text;

class PolicyTest {

  @Test
  void empty_policy() {
    Tree tree = new TestTree();

    Policy policy = new Policy(tree, t -> Collections.emptyList());

    assertThat(policy).isNotNull();
    assertThat(policy.id()).isNotPresent();
    assertThat(policy.version()).isNotPresent();
    assertThat(policy.statement()).isEmpty();
  }

  @Test
  void policy_without_statement() {
    TextTree id = text("666");
    TextTree version = text("12.12.12");
    Tree tree = new TestTree(
      new TestPropertyTree("Id", id),
      new TestPropertyTree("Version", version));

    Policy policy = new Policy(tree, t -> Collections.emptyList());

    assertThat(policy).isNotNull();
    assertThat(policy.id())
      .isPresent()
      .contains(id);

    assertThat(policy.version())
      .isPresent()
      .contains(version);

    assertThat(policy.statement()).isEmpty();
  }

  @Test
  void empty_statement() {
    TextTree statementTree = text("...");
    Tree tree = new TestTree(statementTree);

    Policy policy = new Policy(tree, Tree::children);

    assertThat(policy).isNotNull();
    assertThat(policy.id()).isNotPresent();
    assertThat(policy.version()).isNotPresent();

    assertThat(policy.statement()).hasSize(1);
    Statement statement = policy.statement().get(0);

    assertThat(statement.sid()).isNotPresent();
    assertThat(statement.effect()).isNotPresent();
    assertThat(statement.principal()).isNotPresent();
    assertThat(statement.notPrincipal()).isNotPresent();
    assertThat(statement.action()).isNotPresent();
    assertThat(statement.notAction()).isNotPresent();
    assertThat(statement.resource()).isNotPresent();
    assertThat(statement.notResource()).isNotPresent();
    assertThat(statement.condition()).isNotPresent();
  }

  @Test
  void full_statement() {
    Tree statementTree = new TestTree(
      new TestPropertyTree("Sid", text("sid")),
      new TestPropertyTree("Effect", text("Allow")),
      new TestPropertyTree("Principal", text("principal")),
      new TestPropertyTree("NotPrincipal", text("notPrincipal")),
      new TestPropertyTree("Action", text("action")),
      new TestPropertyTree("NotAction", text("notAction")),
      new TestPropertyTree("Resource", text("resource")),
      new TestPropertyTree("NotResource", text("notResource")),
      new TestPropertyTree("Condition", text("condition")));
    Tree tree = new TestTree(statementTree);

    Policy policy = new Policy(tree, Tree::children);

    assertThat(policy).isNotNull();
    assertThat(policy.id()).isNotPresent();
    assertThat(policy.version()).isNotPresent();

    assertThat(policy.statement()).hasSize(1);
    Statement statement = policy.statement().get(0);

    assertThat(statement.sid()).isPresent();
    assertThat(statement.effect()).isPresent();
    assertThat(statement.principal()).isPresent();
    assertThat(statement.notPrincipal()).isPresent();
    assertThat(statement.action()).isPresent();
    assertThat(statement.notAction()).isPresent();
    assertThat(statement.resource()).isPresent();
    assertThat(statement.notResource()).isPresent();
    assertThat(statement.condition()).isPresent();
  }

  static class TestPropertyTree extends AbstractTestTree implements PropertyTree {

    private final Tree key;
    private final Tree value;

    private TestPropertyTree(String key, Tree value) {
      this.key = text(key);
      this.value = value;
    }

    @Override
    public Tree key() {
      return key;
    }

    @Override
    public Tree value() {
      return value;
    }
  }

  static class TestTree extends AbstractTestTree implements HasProperties {

    private final List<Tree> children = new ArrayList<>();

    private TestTree(Tree... attributes) {
      Stream.of(attributes).forEach(children::add);
    }

    @Override
    public List<Tree> children() {
      return children;
    }

    @Override
    public List<PropertyTree> properties() {
      return children.stream()
        .filter(PropertyTree.class::isInstance)
        .map(PropertyTree.class::cast)
        .collect(Collectors.toList());
    }
  }
}
