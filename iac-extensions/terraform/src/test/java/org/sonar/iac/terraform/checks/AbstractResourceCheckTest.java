/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.parser.HclParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.terraform.TestTreeBuilders.BlockBuilder.block;
import static org.sonar.iac.terraform.TestTreeBuilders.LabelBuilder.label;

class AbstractResourceCheckTest {

  private final HclParser parser = new HclParser();

  @ParameterizedTest
  @CsvSource({
    "resource, true",
    "ephemeral, false",
    "Resource, false",
    "data, false",
    "import, false",
    "moved, false",
    "removed, false",
    "action, false"
  })
  void testIsResource(String type, boolean isResource) {
    BlockTree blockTree = block()
      .key(type)
      .build();
    assertThat(AbstractResourceCheck.isResource(blockTree)).isEqualTo(isResource);
  }

  @ParameterizedTest
  @CsvSource({
    "resource, true",
    "ephemeral, true",
    "data, false",
    "import, false",
    "action_trigger, false"
  })
  void testIsResourceLike(String type, boolean expected) {
    assertThat(AbstractResourceCheck.isResourceLike(block().key(type).build())).isEqualTo(expected);
    assertThat(AbstractResourceCheck.isResourceOrEphemeral(block().key(type).build())).isEqualTo(expected);
  }

  @Test
  void shouldTreatOnlyTopLevelActionBlocksAsResourceLike() {
    BlockTree action = parseBlock("action \"aws_lambda_invoke\" \"rotate\" {}");
    assertThat(AbstractResourceCheck.isAction(action)).isTrue();
    assertThat(AbstractResourceCheck.isResourceLike(action)).isTrue();
    assertThat(AbstractResourceCheck.isResourceOrEphemeral(action)).isFalse();
    assertThat(AbstractResourceCheck.isResource(action)).isFalse();

    BlockTree nestedAction = (BlockTree) parseBlock("""
      resource "aws_wafv2_web_acl" "acl" {
        action {}
      }
      """).properties().get(0);
    assertThat(AbstractResourceCheck.isAction(nestedAction)).isFalse();
    assertThat(AbstractResourceCheck.isResourceLike(nestedAction)).isFalse();
    assertThat(AbstractResourceCheck.isAction(block().key("action").build())).isFalse();
  }

  @Test
  void actionHasResourceLabels() {
    BlockTree action = parseBlock("action \"aws_lambda_invoke\" \"rotate\" {}");
    assertThat(AbstractResourceCheck.getResourceType(action)).isEqualTo("aws_lambda_invoke");
    assertThat(AbstractResourceCheck.hasReferenceLabel(action)).isTrue();
    assertThat(AbstractResourceCheck.getReferenceLabel(action)).isEqualTo("rotate");

    BlockTree unlabeledAction = parseBlock("action {}");
    assertThat(AbstractResourceCheck.getResourceType(unlabeledAction)).isNull();
    assertThat(AbstractResourceCheck.hasReferenceLabel(unlabeledAction)).isFalse();
  }

  @ParameterizedTest
  @CsvSource({
    "\"aws_s3_bucket\", true",
    "\"not_a_bucket\", false",
    "aws_s3_bucket, true"
  })
  void testIsS3Bucket(String label, boolean isS3Bucket) {
    BlockTree blockTree = block()
      .key("resource")
      .labels(label(label))
      .build();
    assertThat(AbstractResourceCheck.isS3Bucket(blockTree)).isEqualTo(isS3Bucket);
  }

  @ParameterizedTest
  @CsvSource({
    "resource, \"aws_s3_bucket\", true",
    "ephemeral, \"aws_s3_bucket\", false",
    "resource, \"not_a_bucket\", false",
    "date, \"aws_s3_bucket\", false"
  })
  void testIsS3Bucket(String type, String label, boolean isS3Bucket) {
    BlockTree blockTree = block()
      .key(type)
      .labels(label(label))
      .build();
    assertThat(AbstractResourceCheck.isS3BucketResource(blockTree)).isEqualTo(isS3Bucket);
  }

  @Test
  void checkResource() {
    TestAbstractResourceCheck check = new TestAbstractResourceCheck();
    TerraformVerifier.verifyNoIssue("AbstractResourceCheck/test.tf", check);
    assertThat(check.visitedBlocks).hasSize(6);
    assertThat(check.visitedS3Buckets)
      .singleElement()
      .satisfies(bucket -> assertThat(AbstractResourceCheck.isResource(bucket)).isTrue());
  }

  @Test
  void ephemeralHasResourceLabels() {
    BlockTree blockTree = block()
      .key("ephemeral")
      .labels(label("\"aws_secretsmanager_secret_version\""), label("\"example\""))
      .build();
    assertThat(AbstractResourceCheck.getResourceType(blockTree)).isEqualTo("aws_secretsmanager_secret_version");
    assertThat(AbstractResourceCheck.hasReferenceLabel(blockTree)).isTrue();
    assertThat(AbstractResourceCheck.getReferenceLabel(blockTree)).isEqualTo("example");
  }

  @Test
  void reportResourceHighlightsKeyOfLabelLessBlocks() {
    var ctx = mock(CheckContext.class);
    BlockTree blockTree = block().key("locals").build();

    AbstractResourceCheck.reportResource(ctx, blockTree, "message");
    AbstractResourceCheck.reportResource(ctx, blockTree, "message", List.of());
    AbstractResourceCheck.reportResource(ctx, blockTree, "message", "context");

    verify(ctx).reportIssue(blockTree.key(), "message");
    verify(ctx).reportIssue(blockTree.key(), "message", List.of());
    verify(ctx).reportIssue(blockTree.key(), "message", List.of(), "context");
  }

  @Test
  void reportResourceHighlightsFirstLabelOfSingleLabelBlocks() {
    var ctx = mock(CheckContext.class);
    BlockTree blockTree = block().key("variable").labels(label("\"example\"")).build();

    AbstractResourceCheck.reportResource(ctx, blockTree, "message");

    verify(ctx).reportIssue(blockTree.labels().get(0), "message");
    assertThat(AbstractResourceCheck.hasReferenceLabel(blockTree)).isFalse();
  }

  private BlockTree parseBlock(String source) {
    return (BlockTree) ((FileTree) parser.parse(source)).properties().get(0);
  }

  static class TestAbstractResourceCheck extends AbstractResourceCheck {

    public final Set<BlockTree> visitedBlocks = new HashSet<>();
    public final Set<BlockTree> visitedS3Buckets = new HashSet<>();

    @Override
    protected void registerResourceChecks() {
      register((ctx, resource) -> visitedBlocks.add(resource));
      register((ctx, resource) -> visitedS3Buckets.add(resource), S3_BUCKET);
    }
  }
}
