/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.common.yaml.tree.MappingTree;
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
  void testGetTrees() {
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
  void testGetSingleTree() {
    assertThat(XPathUtils.getSingleTree(firstDocument, "/Resources/S3BucketPolicy/Properties/PolicyDocument/Statement[]"))
      .isNotPresent();
    assertThat(XPathUtils.getSingleTree(firstDocument, "/Resources/S3BucketPolicy/Properties/PolicyDocument"))
      .isPresent();
  }

  @Test
  void testGetSingleTreeWithCustomRoot() {
    assertThat(XPathUtils.getSingleTree(firstDocument, "/Resources/S3BucketPolicy")).isPresent()
      .satisfies(o -> assertThat(XPathUtils.getSingleTree(o.get(), "/Properties/PolicyDocument")).isPresent());
  }

  @Test
  void testInvalidExpression() {
    assertThatThrownBy(() -> XPathUtils.getSingleTree(firstDocument, "Resources/S3BucketPolicy"))
      .isInstanceOf(XPathUtils.InvalidXPathExpression.class);
  }

  @Test
  void testOnlyRootExpression() {
    assertThat(XPathUtils.getSingleTree(firstDocument, "/")).isPresent().get().isEqualTo(firstDocument);
  }

  @Test
  void testGlobeInTheMiddleOfThePath() {
    assertThat(XPathUtils.getTrees(firstDocument, "/Resources/*/Type"))
      .isNotEmpty().hasSize(2)
      .allMatch(ScalarTree.class::isInstance)
      .extracting(TextUtils::getValue)
      .extracting(Optional::get)
      .containsExactly("AWS::S3::Bucket", "AWS::S3::BucketPolicy");
  }

  @Test
  void testGlobeInTheEndOfThePath() {
    assertThat(XPathUtils.getTrees(firstDocument, "/Resources/S3Bucket/Properties/*"))
      .isNotEmpty().hasSize(2)
      .allMatch(ScalarTree.class::isInstance)
      .extracting(TextUtils::getValue)
      .extracting(Optional::get)
      .containsExactly("mynoncompliantbucket", "Private");
  }

  @Test
  void testGetSingleTreeOfTypeReturnsTreeWhenTypeMatches() {
    Optional<MappingTree> result = XPathUtils.getSingleTreeOfType(firstDocument, "/Resources/S3Bucket/Properties", MappingTree.class);
    assertThat(result).isPresent();
    assertThat(result.get()).isInstanceOf(MappingTree.class);
  }

  @Test
  void testGetSingleTreeOfTypeReturnsEmptyWhenTypeDoesNotMatch() {
    // /Resources/S3Bucket/Properties/BucketName is a ScalarTree, not a MappingTree
    Optional<MappingTree> result = XPathUtils.getSingleTreeOfType(firstDocument, "/Resources/S3Bucket/Properties/BucketName", MappingTree.class);
    assertThat(result).isNotPresent();
  }

  @Test
  void testGetSingleTreeOfTypeReturnsEmptyWhenPathNotFound() {
    Optional<ScalarTree> result = XPathUtils.getSingleTreeOfType(firstDocument, "/NonExistent/Path", ScalarTree.class);
    assertThat(result).isNotPresent();
  }

  @Test
  void testGetTreesOfTypeReturnsOnlyMatchingTypes() {
    // /Resources/S3BucketPolicy/Properties/PolicyDocument/Statement[] returns multiple trees
    // but we filter to only ScalarTree instances
    List<ScalarTree> result = XPathUtils.getTreesOfType(firstDocument, "/Resources/*/Type", ScalarTree.class);
    assertThat(result)
      .hasSize(2)
      .allMatch(ScalarTree.class::isInstance)
      .extracting(TextUtils::getValue)
      .extracting(Optional::get)
      .containsExactly("AWS::S3::Bucket", "AWS::S3::BucketPolicy");
  }

  @Test
  void testGetTreesOfTypeReturnsEmptyWhenNoTypeMatches() {
    // /Resources/*/Type returns ScalarTree instances, not MappingTree
    List<MappingTree> result = XPathUtils.getTreesOfType(firstDocument, "/Resources/*/Type", MappingTree.class);
    assertThat(result).isEmpty();
  }

  @Test
  void testGetTreesOfTypeReturnsEmptyWhenPathNotFound() {
    List<ScalarTree> result = XPathUtils.getTreesOfType(firstDocument, "/NonExistent/Path[]", ScalarTree.class);
    assertThat(result).isEmpty();
  }

}
