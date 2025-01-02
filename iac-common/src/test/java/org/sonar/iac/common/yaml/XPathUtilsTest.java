/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.yaml;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XPathUtilsTest {

  public static final Path BASE_DIR = Paths.get("src", "test", "resources");

  private YamlTree firstDocument;

  @BeforeEach
  void setUp() {
    TestCheck check = new TestCheck();
    Verifier.verifyNoIssue(new YamlParser(), BASE_DIR.resolve("XPathUtilsTest/test.yaml"), check);
    this.firstDocument = check.firstDocument;
  }

  @Test
  void test_getTrees() {
    assertThat(XPathUtils.getTrees(firstDocument, "/Resources/S3BucketPolicy/Properties/PolicyDocument/Statement[]/Principal/AWS"))
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
    assertThat(XPathUtils.getSingleTree(firstDocument, "/Resources/S3BucketPolicy/Properties/PolicyDocument/Statement[]"))
      .isNotPresent();
    assertThat(XPathUtils.getSingleTree(firstDocument, "/Resources/S3BucketPolicy/Properties/PolicyDocument"))
      .isPresent();
  }

  @Test
  void test_getSingleTree_with_custom_root() {
    assertThat(XPathUtils.getSingleTree(firstDocument, "/Resources/S3BucketPolicy")).isPresent()
      .satisfies(o -> assertThat(XPathUtils.getSingleTree(o.get(), "/Properties/PolicyDocument")).isPresent());
  }

  @Test
  void test_invalid_expression() {
    assertThatThrownBy(() -> XPathUtils.getSingleTree(firstDocument, "Resources/S3BucketPolicy"))
      .isInstanceOf(XPathUtils.InvalidXPathExpression.class);
  }

  @Test
  void test_only_root_expression() {
    assertThat(XPathUtils.getSingleTree(firstDocument, "/")).isPresent().get().isEqualTo(firstDocument);
  }

}
