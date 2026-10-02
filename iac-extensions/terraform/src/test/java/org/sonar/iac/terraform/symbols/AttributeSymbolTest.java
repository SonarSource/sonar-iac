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
package org.sonar.iac.terraform.symbols;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeSymbolTest extends AbstractSymbolTest {

  @Test
  void reportFromPresent() {
    AttributeTree tree = parseAttribute("my_attribute = 1");
    AttributeSymbol attribute = AttributeSymbol.fromPresent(ctx, tree, parentBlock);
    attribute.report("message");
    assertIssueReported(tree, "message");
  }

  @Test
  void reportFromAbsent() {
    AttributeSymbol attribute = AttributeSymbol.fromAbsent(ctx, "my_attribute", parentBlock);
    attribute.report("message");
    assertNoIssueReported();
  }

  @Test
  void reportIfAbsentFromAbsent() {
    AttributeSymbol attribute = AttributeSymbol.fromAbsent(ctx, "my_attribute", parentBlock);
    attribute.reportIfAbsent("%s");
    assertIssueReported(parentBlock.tree.key(), "my_attribute");
  }

  @Test
  void reportIfAbsentFromPresent() {
    AttributeTree tree = parseAttribute("my_attribute = 1");
    AttributeSymbol attribute = AttributeSymbol.fromPresent(ctx, tree, parentBlock);
    attribute.reportIfAbsent("%s");
    assertNoIssueReported();
  }

  @Test
  void reportIfFromPresent() {
    AttributeTree tree = parseAttribute("my_attribute = 1");
    AttributeSymbol attribute = AttributeSymbol.fromPresent(ctx, tree, parentBlock);
    attribute.reportIf(e -> true, "message");
    assertIssueReported(tree, "message");
  }

  @Test
  void reportIfFromAbsent() {
    AttributeSymbol attribute = AttributeSymbol.fromAbsent(ctx, "my_attribute", parentBlock);
    attribute.reportIf(e -> true, "message");
    assertNoIssueReported();
  }

  @Test
  void reportIfNotMatchingPredicate() {
    AttributeTree tree = parseAttribute("my_attribute = 1");
    AttributeSymbol attribute = AttributeSymbol.fromPresent(ctx, tree, parentBlock);
    attribute.reportIf(e -> false, "message");
    assertNoIssueReported();
  }

  @Test
  void writeOnlyArgumentAndVersion() {
    BlockTree tree = parseBlock("""
      resource "aws_db_instance" "db" {
        password_wo = "secret"
        password_wo_version = 1
      }
      """);
    ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, tree);
    assertThat(resource.attribute("password_wo").isWriteOnly()).isTrue();
    assertThat(resource.attribute("password_wo_version").isWriteOnly()).isFalse();
    assertThat(resource.attribute("password_wo_version").isWriteOnlyVersion()).isTrue();
    assertThat(resource.attribute("missing_wo").isWriteOnly()).isFalse();

    ResourceSymbol missingPair = ResourceSymbol.fromPresent(ctx, parseBlock("resource \"custom_resource\" \"x\" { token_wo_version = 1 }"));
    assertThat(missingPair.attribute("token_wo_version").isWriteOnlyVersion()).isFalse();
  }

  @Test
  void writeOnlyArgumentMustHaveABaseName() {
    ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, parseBlock("""
      resource "custom_resource" "x" {
        _wo = "value"
        token = "value"
      }
      """));
    assertThat(resource.attribute("_wo").isWriteOnly()).isFalse();
    assertThat(resource.attribute("token").isWriteOnly()).isFalse();
    assertThat(resource.attribute("missing_wo_version").isWriteOnlyVersion()).isFalse();
    assertThat(resource.attribute("token").isWriteOnlyVersion()).isFalse();
  }

  @Test
  void writeOnlyArgumentsAreNotVariableAttributes() {
    BlockSymbol variable = BlockSymbol.fromPresent(ctx, parseBlock("variable \"token\" { token_wo = \"value\" }"), null);
    assertThat(variable.attribute("token_wo").isWriteOnly()).isFalse();
    assertThat(variable.attribute("token_wo").isWriteOnlyVersion()).isFalse();
  }

  @Test
  void writeOnlyArgumentsAreNotDataSourceAttributes() {
    ResourceSymbol data = ResourceSymbol.fromPresent(ctx, parseBlock("""
      data "custom_resource" "x" {
        token_wo = "value"
        token_wo_version = 1
      }
      """));
    assertThat(data.attribute("token_wo").isWriteOnly()).isFalse();
    assertThat(data.attribute("token_wo_version").isWriteOnlyVersion()).isFalse();
  }

  @Test
  void onlyPresentAttributesInsideAStoreCarrySensitiveContent() {
    ResourceSymbol data = ResourceSymbol.fromPresent(ctx, parseBlock("""
      resource "terraform_data" "x" {
        store "credentials" {
          value = "secret"
          nested {
            value = "another secret"
          }
        }
        input = "ordinary"
      }
      """));
    assertThat(data.block("store").attribute("value").isSensitiveStoreContent()).isTrue();
    assertThat(data.block("store").block("nested").attribute("value").isSensitiveStoreContent()).isTrue();
    assertThat(data.block("store").attribute("missing").isSensitiveStoreContent()).isFalse();
    assertThat(data.attribute("input").isSensitiveStoreContent()).isFalse();

    BlockSymbol ordinary = BlockSymbol.fromPresent(ctx, parseBlock("ordinary { value = \"plain\" }"), null);
    assertThat(ordinary.attribute("value").isSensitiveStoreContent()).isFalse();
  }
}
