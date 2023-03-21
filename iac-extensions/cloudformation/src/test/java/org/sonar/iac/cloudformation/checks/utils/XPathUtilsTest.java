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
package org.sonar.iac.cloudformation.checks.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.checks.CloudformationVerifier;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XPathUtilsTest extends AbstractUtilsTest {

  private YamlTree root;

  @BeforeEach
  void setUp() {
    TestCheck check = new TestCheck();
    CloudformationVerifier.verifyNoIssue("AbstractXPathCheck/test.yaml", check);
    this.root = check.root;
  }

  @Test
  void test_getTrees() {
    assertThat(XPathUtils.getTrees(root, "/Resources/S3BucketPolicy/Properties/PolicyDocument/Statement[]/Principal/AWS"))
      .isNotEmpty().hasSize(1)
      .satisfies(t -> {
        YamlTree tree = t.get(0);
        assertThat(tree).isInstanceOfSatisfying(SequenceTree.class, s -> assertThat(s.elements()).hasSize(1).satisfies(els -> {
          YamlTree element = els.get(0);
          assertThat(element).isInstanceOfSatisfying(ScalarTree.class, v -> assertThat(v.value()).isEqualTo("arn:aws:iam::123456789123:root"));
        }));
      });
  }

  @Test
  void test_getSingleTree() {
    assertThat(XPathUtils.getSingleTree(root, "/Resources/S3BucketPolicy/Properties/PolicyDocument/Statement[]"))
      .isNotPresent();
    assertThat(XPathUtils.getSingleTree(root, "/Resources/S3BucketPolicy/Properties/PolicyDocument"))
      .isPresent();
  }

  @Test
  void test_getSingleTree_with_custom_root() {
    assertThat(XPathUtils.getSingleTree(root, "/Resources/S3BucketPolicy")).isPresent()
      .satisfies(o -> assertThat(XPathUtils.getSingleTree(o.get(), "/Properties/PolicyDocument")).isPresent());
  }

  @Test
  void test_invalid_expression() {
    assertThatThrownBy(() -> XPathUtils.getSingleTree(root, "Resources/S3BucketPolicy"))
      .isInstanceOf(XPathUtils.InvalidXPathExpression.class);
  }

  @Test
  void test_only_root_expression() {
    assertThat(XPathUtils.getSingleTree(root, "/")).isPresent().get().isEqualTo(root);
  }

}
