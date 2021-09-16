/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks.utils;

import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.checks.CloudformationVerifier;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.Policy.Statement;

import static org.sonar.iac.cloudformation.checks.utils.PolicyUtils.getPolicies;
import static org.assertj.core.api.Assertions.assertThat;

class PolicyUtilsTest {

  @Test
  void test_policy() {
    assertThat(getPolicies(of("AbstractPolicyCheck/policy.yaml")))
      .hasSize(1)
      .satisfies(p -> { Policy policy = p.get(0);
        assertThat(policy.id()).isPresent().get().
          isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("id"));
        assertThat(policy.version()).isPresent().get().
          isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("v"));
        assertThat(policy.statement())
          .hasSize(1);
      });
  }

  @Test
  void test_policies() {
    assertThat(getPolicies(of("AbstractPolicyCheck/policies.yaml")))
      .hasSize(2)
      .satisfies(p -> {
        assertThat(p.get(0).statement())
          .hasSize(2);
        assertThat(p.get(1).statement())
          .hasSize(1);
      });
  }

  @Test
  void test_deep() {
    assertThat(getPolicies(of("AbstractPolicyCheck/deep.yaml")))
      .hasSize(1);
  }

  @Test
  void test_statement() {
    assertThat(getPolicies(of("AbstractPolicyCheck/statement.yaml")))
      .hasSize(1)
      .satisfies(p -> { Policy policy = p.get(0);
        assertThat(policy.statement())
          .hasSize(1)
          .satisfies(s -> { Statement statement = s.get(0);
            assertThat(statement.sid()).isPresent().get().
              isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("sid"));
            assertThat(statement.effect()).isPresent().get().
              isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("eff"));
            assertThat(statement.principal()).isPresent().get().
              isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("pri"));
            assertThat(statement.notPrincipal()).isPresent().get().
              isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("npr"));
            assertThat(statement.action()).isPresent().get().
              isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("act"));
            assertThat(statement.notAction()).isPresent().get().
              isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("nac"));
            assertThat(statement.resource()).isPresent().get().
              isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("rsc"));
            assertThat(statement.notResource()).isPresent().get().
              isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("nrs"));
            assertThat(statement.condition()).isPresent().get().
              isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("cnd"));
          });
      });
  }

  @Test
  void test_malformed() {
    assertThat(getPolicies(of("AbstractPolicyCheck/malformed.yaml")))
      .isEmpty();
  }

  private static CloudformationTree of(String filename) {
    TestPolicyCheck check = new TestPolicyCheck();
    CloudformationVerifier.verifyNoIssue(filename, check);
    return check.root;
  }

  private static class TestPolicyCheck implements IacCheck {
    private CloudformationTree root;

    @Override
    public void initialize(InitContext init) {
      init.register(FileTree.class, (ctx, tree) -> {
        this.root = tree.root();
      });
    }
  }
}
