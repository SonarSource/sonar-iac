/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.yaml.block;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.yaml.YamlTreeTest;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BlockBlockTest extends YamlTreeTest {

  CheckContext ctx = mock(CheckContext.class);

  @Test
  void fromPresent() {
    MappingTree tree = parseMap("a: b");
    BlockBlock block = BlockBlock.fromPresent(ctx, tree, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block.status).isEqualTo(YamlBlock.Status.PRESENT);
    assertThat(block.tree).isEqualTo(tree);
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void fromPresent_unknown() {
    YamlTree tree = parse("a:b", YamlTree.class);
    BlockBlock block = BlockBlock.fromPresent(ctx, tree, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block.status).isEqualTo(YamlBlock.Status.UNKNOWN);
    assertThat(block.tree).isNull();
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void fromAbsent() {
    BlockBlock block = BlockBlock.fromAbsent(ctx, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block.status).isEqualTo(YamlBlock.Status.ABSENT);
    assertThat(block.tree).isNull();
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void blocks() {
    BlockBlock block = BlockBlock.fromPresent(ctx, parseMap("foo:\n - key: value"), "a");
    List<BlockBlock> presentBlocks = block.blocks("foo").collect(Collectors.toList());

    assertThat(presentBlocks).hasSize(1);
    assertThat(presentBlocks.get(0).key).isEqualTo("foo");
  }

  @Test
  void block() {
    BlockBlock block = BlockBlock.fromPresent(ctx, parseMap("foo:\n key: value"), "a");
    BlockBlock presentBlock = block.block("foo");
    assertThat(presentBlock.status).isEqualTo(YamlBlock.Status.PRESENT);

    BlockBlock absentBlock = block.block("bar");
    assertThat(absentBlock.status).isEqualTo(YamlBlock.Status.ABSENT);
  }

  @Test
  void attribute() {
    BlockBlock block = BlockBlock.fromPresent(ctx, parseMap("foo: bar"), "a");
    AttributeBlock presentAttr = block.attribute("foo");
    assertThat(presentAttr.status).isEqualTo(YamlBlock.Status.PRESENT);

    AttributeBlock absentAttr = block.attribute("bar");
    assertThat(absentAttr.status).isEqualTo(YamlBlock.Status.ABSENT);
  }

  @Test
  void list() {
    BlockBlock block = BlockBlock.fromPresent(ctx, parseMap("foo: [bar, car]"), "a");
    ListBlock listPresent = block.list("foo");
    assertThat(listPresent.status).isEqualTo(YamlBlock.Status.PRESENT);
    assertThat(listPresent.items.stream()
      .map(tree -> ((ScalarTree) tree).value()))
        .containsExactly("bar", "car");

    ListBlock listAbsent = block.list("bar");
    assertThat(listAbsent.items).isEmpty();
    assertThat(listAbsent.status).isEqualTo(YamlBlock.Status.ABSENT);
  }

  public MappingTree parseMap(String source) {
    return parse(source, MappingTree.class);
  }
}
