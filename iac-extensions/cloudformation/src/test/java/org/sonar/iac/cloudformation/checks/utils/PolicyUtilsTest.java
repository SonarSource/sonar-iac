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
package org.sonar.iac.cloudformation.checks.utils;

import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.checks.CloudformationVerifier;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.Policy.Statement;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.cloudformation.checks.utils.PolicyUtils.getPolicies;

class PolicyUtilsTest extends AbstractUtilsTest {

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

  private static YamlTree of(String filename) {
    TestCheck check = new TestCheck();
    CloudformationVerifier.verifyNoIssue(filename, check);
    return check.root;
  }
}
