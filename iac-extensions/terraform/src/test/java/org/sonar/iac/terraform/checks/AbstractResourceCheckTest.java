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
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.terraform.api.tree.BlockTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.BlockBuilder.block;
import static org.sonar.iac.terraform.TestTreeBuilders.LabelBuilder.label;

class AbstractResourceCheckTest {

  @ParameterizedTest
  @CsvSource({
    "resource, true",
    "ephemeral, false",
    "Resource, false",
    "data, false",
    "import, false",
    "moved, false",
    "removed, false"
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
    "import, false"
  })
  void testIsResourceOrEphemeral(String type, boolean expected) {
    assertThat(AbstractResourceCheck.isResourceOrEphemeral(block().key(type).build())).isEqualTo(expected);
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
    assertThat(check.visitedBlocks).hasSize(3);
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

  static class TestAbstractResourceCheck extends AbstractResourceCheck {

    public final Set<BlockTree> visitedBlocks = new HashSet<>();

    @Override
    protected void registerResourceChecks() {
      register((ctx, resource) -> visitedBlocks.add(resource));
    }
  }
}
