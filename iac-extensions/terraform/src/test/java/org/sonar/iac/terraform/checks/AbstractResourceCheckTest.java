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
package org.sonar.iac.terraform.checks;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.terraform.api.tree.BlockTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.BlockBuilder.block;
import static org.sonar.iac.terraform.TestTreeBuilders.LabelBuilder.label;

class AbstractResourceCheckTest {

  @ParameterizedTest
  @CsvSource({
    "resource, true",
    "Resource, false",
    "data, false"
  })
  void test_isResource(String type, boolean isS3Bucket) {
    BlockTree blockTree = block()
      .key(type)
      .build();
    assertThat(AbstractResourceCheck.isResource(blockTree)).isEqualTo(isS3Bucket);
  }

  @ParameterizedTest
  @CsvSource({
    "\"aws_s3_bucket\", true",
    "\"not_a_bucket\", false",
    "aws_s3_bucket, true"
  })
  void test_isS3Bucket(String label, boolean isS3Bucket) {
    BlockTree blockTree = block()
      .labels(label(label))
      .build();
    assertThat(AbstractResourceCheck.isS3Bucket(blockTree)).isEqualTo(isS3Bucket);
  }

  @ParameterizedTest
  @CsvSource({
    "resource, \"aws_s3_bucket\", true",
    "resource, \"not_a_bucket\", false",
    "date, \"aws_s3_bucket\", false"
  })
  void test_isS3Bucket(String type, String label, boolean isS3Bucket) {
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
    assertThat(check.visitedBlocks).hasSize(2);
  }

  static class TestAbstractResourceCheck extends AbstractResourceCheck {

    public final Set<BlockTree> visitedBlocks = new HashSet<>();

    @Override
    protected void registerResourceChecks() {
      register((ctx, resource) -> visitedBlocks.add(resource));
    }
  }
}
