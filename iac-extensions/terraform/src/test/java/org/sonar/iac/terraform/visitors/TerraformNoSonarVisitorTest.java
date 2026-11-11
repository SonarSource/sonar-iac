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
package org.sonar.iac.terraform.visitors;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.iac.common.extension.visitors.NoSonarVisitor;
import org.sonar.iac.common.testing.AbstractNoSonarVisitorTest;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.iac.terraform.plugin.TerraformLanguage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TerraformNoSonarVisitorTest extends AbstractNoSonarVisitorTest {

  @Override
  protected HclParser treeParser() {
    return new HclParser();
  }

  @Override
  protected NoSonarVisitor noSonarVisitor(NoSonarFilter noSonarFilter) {
    return new TerraformNoSonarVisitor(noSonarFilter);
  }

  @Override
  protected String languageKey() {
    return TerraformLanguage.KEY;
  }

  @Test
  void shouldNotRegisterAnythingForEmptySource() {
    scan("");
    assertThat(visitor.noSonarLines()).isEmpty();
    verify(noSonarFilter, never()).noSonarInFile(any(), any());
  }

  @Test
  void shouldRegisterLineOfNoSonarComment() {
    scan("""
      a {
      // NOSONAR comment
      b = {}
      }""");
    assertThat(visitor.noSonarLines()).containsExactly(2);
    verify(noSonarFilter).noSonarInFile(inputFile, Set.of(2));
  }

  @Test
  void shouldCoverWholeHeredocWithFullLineNoSonarCommentAboveIt() {
    scan("""
      resource "aws_s3_bucket_policy" "example" {
        # NOSONAR
        policy = <<EOF
      {
        "Statement": [{ "Effect": "Allow", "Principal": "*" }]
      }
      EOF
        bucket = "example"
      }""");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(2, 3, 4, 5, 6, 7);
    verify(noSonarFilter).noSonarInFile(inputFile, Set.of(2, 3, 4, 5, 6, 7));
  }

  @Test
  void shouldCoverWholeHeredocWithNoSonarCommentOnHeredocStartLine() {
    scan("""
      resource "aws_s3_bucket_policy" "example" {
        policy = /* NOSONAR */ <<EOF
      { "Statement": [{ "Effect": "Allow", "Principal": "*" }] }
      EOF
      }""");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(2, 3, 4);
  }

  @Test
  void shouldNotCoverNextLineWithFullLineNoSonarCommentAboveOtherCode() {
    scan("""
      resource "aws_s3_bucket" "example" {
        # NOSONAR
        acl = "public-read"
        policy = <<EOF
      { "Statement": [] }
      EOF
      }""");
    assertThat(visitor.noSonarLines()).containsExactly(2);
  }
}
