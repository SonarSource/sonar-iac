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
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.TupleTree;

import static org.assertj.core.api.Assertions.assertThat;

class BlockSymbolTest extends AbstractSymbolTest {

  @Test
  void reportFromPresent() {
    BlockTree tree = parseBlock("my_block {}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.report("message");
    assertIssueReported(tree.key(), "message");
  }

  @Test
  void reportFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.report("message");
    assertNoIssueReported();
  }

  @Test
  void reportIfAbsentFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.reportIfAbsent("%s");
    assertIssueReported(parentBlock.tree.key(), "my_block");
  }

  @Test
  void reportIfAbsentFromPresent() {
    BlockTree tree = parseBlock("my_block {}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.reportIfAbsent("%s");
    assertNoIssueReported();
  }

  @Test
  void reportIfAbsentOfBlockWithoutParent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", null);
    block.reportIfAbsent("%s");
    assertNoIssueReported();
  }

  @Test
  void attributeFromPresent() {
    BlockTree tree = parseBlock("my_block {my_attribute = 1}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    assertThat(block.attribute("my_attribute").isPresent()).isTrue();
    assertThat(block.attribute("unknown_attribute").isPresent()).isFalse();
  }

  @Test
  void attributeFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    assertThat(block.attribute("my_attribute").isPresent()).isFalse();
  }

  @Test
  void reportIfAbsentOnAttributeInBlockFromPresent() {
    BlockTree tree = parseBlock("my_block {}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.attribute("my_attribute").reportIfAbsent("%s");
    assertIssueReported(tree.key(), "my_attribute");
  }

  @Test
  void reportIfAbsentOnAttributeInBlockFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.attribute("my_attribute").reportIfAbsent("%s");
    assertNoIssueReported();
  }

  @Test
  void reportOnAttributeInBlockFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.attribute("my_attribute").report("%s");
    assertNoIssueReported();
  }

  @Test
  void reportOnAttributeInBlockFromPresent() {
    BlockTree tree = parseBlock("my_block {my_attribute = 1}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    SecondaryLocation secondary = block.toSecondary("secondary");
    block.attribute("my_attribute").report("message", secondary);
    assertIssueReported(tree.value().statements().get(0), "message", secondary);
  }

  @Test
  void blockFromPresent() {
    BlockTree tree = parseBlock("my_block {\nchild_block {}\n}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    assertThat(block.block("child_block").isPresent()).isTrue();
    assertThat(block.block("unknown_block").isPresent()).isFalse();
    assertThat(block.toSecondary("secondary")).isEqualTo(new SecondaryLocation(tree.key(), "secondary"));
  }

  @Test
  void blockFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    assertThat(block.block("child_block").isPresent()).isFalse();
    assertThat(block.toSecondary("secondary")).isNull();
  }

  @Test
  void reportIfAbsentOnBlockInBlockFromPresent() {
    BlockTree tree = parseBlock("my_block {}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.block("child_block").reportIfAbsent("%s");
    assertIssueReported(tree.key(), "child_block");
  }

  @Test
  void reportIfAbsentOnBlockInBlockFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.block("child_block").reportIfAbsent("%s");
    assertNoIssueReported();
  }

  @Test
  void reportOnBlockInBlockFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.block("my_block").report("%s");
    assertNoIssueReported();
  }

  @Test
  void reportOnBlockInBlockFromPresent() {
    BlockTree tree = parseBlock("my_block {\nchild_block {}\n}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.block("child_block").report("message");
    assertIssueReported(tree.value().statements().get(0).key(), "message");
  }

  @Test
  void blocksFromPresent() {
    BlockTree tree = parseBlock("my_block {\nchild_block {}\nchild_block {}\n}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    assertThat(block.blocks("child_block")).hasSize(2);
    assertThat(block.blocks("unknown")).isEmpty();
  }

  @Test
  void blocksFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    assertThat(block.blocks("child_block")).isEmpty();
  }

  @Test
  void listFromPresent() {
    BlockTree tree = parseBlock("my_block {my_list = [\"my_itm\"]}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    assertThat(block.list("my_list").isPresent()).isTrue();
    assertThat(block.list("unknown").isPresent()).isFalse();
  }

  @Test
  void listFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    assertThat(block.list("my_list").isPresent()).isFalse();
  }

  @Test
  void reportItemIfOnListInBlockFromPresent() {
    BlockTree tree = parseBlock("my_block {my_list = [\"my_itm\"]}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.list("my_list").reportItemIf(e -> true, "message");
    assertIssueReported(((TupleTree) tree.value().statements().get(0).value()).elements().trees().get(0), "message");
  }

  @Test
  void reportItemIfOnListInBlockFromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    assertThat(block.list("my_list").isPresent()).isFalse();
    assertNoIssueReported();
  }

  @Test
  void ephemeralDeclarations() {
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("ephemeral \"aws_secretsmanager_secret_version\" \"db\" {}"), null).isEphemeral()).isTrue();
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("variable \"password\" { ephemeral = true }"), null).isEphemeral()).isTrue();
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("output \"password\" { ephemeral = true }"), null).isEphemeral()).isTrue();
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("variable \"password\" { ephemeral = false }"), null).isEphemeral()).isFalse();
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("output \"password\" { ephemeral = var.enabled }"), null).isEphemeral()).isFalse();
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("resource \"aws_db_instance\" \"db\" { ephemeral = true }"), null).isEphemeral()).isFalse();
    assertThat(BlockSymbol.fromAbsent(ctx, "variable", null).isEphemeral()).isFalse();
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("ephemeral \"type\" {}"), null).isEphemeral()).isFalse();
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("ephemeral \"type\" \"name\" \"extra\" {}"), null).isEphemeral()).isFalse();
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("variable {}"), null).isEphemeral()).isFalse();
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("variable \"password\" {}"), null).isEphemeral()).isFalse();
    assertThat(BlockSymbol.fromPresent(ctx, parseBlock("locals { ephemeral = true }"), null).isEphemeral()).isFalse();
  }

  @Test
  void nestedDeclarationsAreNotEphemeral() {
    BlockTree outerTree = parseBlock("""
      outer {
        ephemeral "type" "name" {}
        variable "password" { ephemeral = true }
        output "password" { ephemeral = true }
      }
      """);
    BlockSymbol outer = BlockSymbol.fromPresent(ctx, outerTree, null);
    assertThat(outer.block("ephemeral").isEphemeral()).isFalse();
    assertThat(outer.block("variable").isEphemeral()).isFalse();
    assertThat(outer.block("output").isEphemeral()).isFalse();
    assertThat(BlockSymbol.fromPresent(ctx, (BlockTree) outerTree.value().statements().get(0), null).isEphemeral()).isFalse();
  }

  @Test
  void terraformDataStoreAndNestedContent() {
    BlockTree tree = parseBlock("""
      resource "terraform_data" "credentials" {
        store "db" {
          value = "secret"
          nested {
            value = "another secret"
          }
        }
      }
      """);
    ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, tree);
    BlockSymbol store = resource.block("store");
    assertThat(store.isSensitiveStore()).isTrue();
    assertThat(store.block("nested").isSensitiveStoreContent()).isTrue();
    assertThat(store.block("missing").isSensitiveStoreContent()).isFalse();
    assertThat(resource.block("missing").isSensitiveStore()).isFalse();
    assertThat(resource.isSensitiveStoreContent()).isFalse();
  }

  @Test
  void storeOutsideTerraformDataIsNotSensitive() {
    ResourceSymbol other = ResourceSymbol.fromPresent(ctx, parseBlock("""
      resource "aws_db_instance" "db" {
        store {}
      }
      """));
    assertThat(other.block("store").isSensitiveStore()).isFalse();

    ResourceSymbol data = ResourceSymbol.fromPresent(ctx, parseBlock("""
      data "terraform_data" "credentials" {
        store {}
      }
      """));
    assertThat(data.block("store").isSensitiveStore()).isFalse();

    BlockSymbol unrelated = BlockSymbol.fromPresent(ctx, parseBlock("""
      outer {
        store {}
      }
      """), null);
    assertThat(unrelated.block("store").isSensitiveStore()).isFalse();
    assertThat(unrelated.block("store").isSensitiveStoreContent()).isFalse();
  }
}
