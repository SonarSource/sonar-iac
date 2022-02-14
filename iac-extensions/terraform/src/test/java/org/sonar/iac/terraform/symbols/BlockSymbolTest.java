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
package org.sonar.iac.terraform.symbols;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.TupleTree;

import static org.assertj.core.api.Assertions.assertThat;


class BlockSymbolTest extends AbstractSymbolTest {

  @Test
  void report_fromPresent() {
    BlockTree tree = parseBlock("my_block {}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.report("message");
    assertIssueReported(tree.key(), "message");
  }

  @Test
  void report_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.report("message");
    assertNoIssueReported();
  }

  @Test
  void reportIfAbsent_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.reportIfAbsent("%s");
    assertIssueReported(parentBlock.tree.key(), "my_block");
  }

  @Test
  void reportIfAbsent_fromPresent() {
    BlockTree tree = parseBlock("my_block {}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.reportIfAbsent("%s");
    assertNoIssueReported();
  }

  @Test
  void reportIfAbsent_of_block_without_parent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", null);
    block.reportIfAbsent("%s");
    assertNoIssueReported();
  }

  @Test
  void attribute_fromPresent() {
    BlockTree tree = parseBlock("my_block {my_attribute = 1}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    assertThat(block.attribute("my_attribute").isPresent()).isTrue();
    assertThat(block.attribute("unknown_attribute").isPresent()).isFalse();
  }

  @Test
  void attribute_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    assertThat(block.attribute("my_attribute").isPresent()).isFalse();
  }

  @Test
  void reportIfAbsent_on_attribute_in_block_fromPresent() {
    BlockTree tree = parseBlock("my_block {}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.attribute("my_attribute").reportIfAbsent("%s");
    assertIssueReported(tree.key(), "my_attribute");
  }

  @Test
  void reportIfAbsent_on_attribute_in_block_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.attribute("my_attribute").reportIfAbsent("%s");
    assertNoIssueReported();
  }

  @Test
  void report_on_attribute_in_block_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.attribute("my_attribute").report("%s");
    assertNoIssueReported();
  }

  @Test
  void report_on_attribute_in_block_fromPresent() {
    BlockTree tree = parseBlock("my_block {my_attribute = 1}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    SecondaryLocation secondary = block.toSecondary("secondary");
    block.attribute("my_attribute").report("message", secondary);
    assertIssueReported(tree.value().statements().get(0), "message", secondary);
  }

  @Test
  void block_fromPresent() {
    BlockTree tree = parseBlock("my_block {\nchild_block {}\n}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    assertThat(block.block("child_block").isPresent()).isTrue();
    assertThat(block.block("unknown_block").isPresent()).isFalse();
    assertThat(block.toSecondary("secondary")).isEqualTo(new SecondaryLocation(tree.key(), "secondary"));
  }

  @Test
  void block_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    assertThat(block.block("child_block").isPresent()).isFalse();
    assertThat(block.toSecondary("secondary")).isNull();
  }

  @Test
  void reportIfAbsent_on_block_in_block_fromPresent() {
    BlockTree tree = parseBlock("my_block {}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.block("child_block").reportIfAbsent("%s");
    assertIssueReported(tree.key(), "child_block");
  }

  @Test
  void reportIfAbsent_on_block_in_block_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.block("child_block").reportIfAbsent("%s");
    assertNoIssueReported();
  }

  @Test
  void report_on_block_in_block_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    block.block("my_block").report("%s");
    assertNoIssueReported();
  }

  @Test
  void report_on_block_in_block_fromPresent() {
    BlockTree tree = parseBlock("my_block {\nchild_block {}\n}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.block("child_block").report("message");
    assertIssueReported(tree.value().statements().get(0).key(), "message");
  }

  @Test
  void blocks_fromPresent() {
    BlockTree tree = parseBlock("my_block {\nchild_block {}\nchild_block {}\n}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    assertThat(block.blocks("child_block")).hasSize(2);
    assertThat(block.blocks("unknown")).isEmpty();
  }

  @Test
  void blocks_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    assertThat(block.blocks("child_block")).isEmpty();
  }

  @Test
  void list_fromPresent() {
    BlockTree tree = parseBlock("my_block {my_list = [\"my_itm\"]}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    assertThat(block.list("my_list").isPresent()).isTrue();
    assertThat(block.list("unknown").isPresent()).isFalse();
  }

  @Test
  void list_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    assertThat(block.list("my_list").isPresent()).isFalse();
  }

  @Test
  void reportItemIf_on_list_in_block_fromPresent() {
    BlockTree tree = parseBlock("my_block {my_list = [\"my_itm\"]}");
    BlockSymbol block = BlockSymbol.fromPresent(ctx, tree, parentBlock);
    block.list("my_list").reportItemIf(e -> true, "message");
    assertIssueReported(((TupleTree) tree.value().statements().get(0).value()).elements().trees().get(0), "message");
  }

  @Test
  void reportItemIf_on_list_in_block_fromAbsent() {
    BlockSymbol block = BlockSymbol.fromAbsent(ctx, "my_block", parentBlock);
    assertThat(block.list("my_list").isPresent()).isFalse();
    assertNoIssueReported();
  }
}
