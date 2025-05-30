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
package org.sonar.iac.common.yaml.object;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.yaml.YamlTreeTest;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BlockObjectTest extends YamlTreeTest {

  CheckContext ctx = mock(CheckContext.class);

  @Test
  void shouldVerifyFromPresent() {
    MappingTree tree = parseMap("a: b");
    BlockObject block = BlockObject.fromPresent(ctx, tree, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block.status).isEqualTo(YamlObject.Status.PRESENT);
    assertThat(block.tree).isEqualTo(tree);
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void shouldVerifyFromPresentUnknown() {
    YamlTree tree = parse("a:b", YamlTree.class);
    BlockObject block = BlockObject.fromPresent(ctx, tree, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block.status).isEqualTo(YamlObject.Status.UNKNOWN);
    assertThat(block.tree).isNull();
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void shouldVerifyFromAbsent() {
    BlockObject block = BlockObject.fromAbsent(ctx, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block.status).isEqualTo(YamlObject.Status.ABSENT);
    assertThat(block.tree).isNull();
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void shouldVerifyBlocks() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo:\n - key: value"), "a");
    List<BlockObject> presentBlocks = block.blocks("foo").toList();

    assertThat(presentBlocks).hasSize(1);
    assertThat(presentBlocks.get(0).key).isEqualTo("foo");
  }

  @Test
  void shouldVerifyChildrenBlocks() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo:\nbar:"), "a");
    List<BlockObject> presentBlocks = block.childrenBlocks().toList();

    assertThat(presentBlocks).hasSize(2);
    assertThat(presentBlocks.get(0).key).isEqualTo("foo");
    assertThat(presentBlocks.get(1).key).isEqualTo("bar");
  }

  @Test
  void shouldVerifyBlock() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo:\n key: value"), "a");
    BlockObject presentBlock = block.block("foo");
    assertThat(presentBlock.status).isEqualTo(YamlObject.Status.PRESENT);

    BlockObject absentBlock = block.block("bar");
    assertThat(absentBlock.status).isEqualTo(YamlObject.Status.ABSENT);
  }

  @Test
  void shouldVerifyAttribute() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo: bar"), "a");
    AttributeObject presentAttr = block.attribute("foo");
    assertThat(presentAttr.status).isEqualTo(YamlObject.Status.PRESENT);

    AttributeObject absentAttr = block.attribute("bar");
    assertThat(absentAttr.status).isEqualTo(YamlObject.Status.ABSENT);
  }

  @Test
  void shouldVerifyList() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo: [bar, car]"), "a");
    ListObject listPresent = block.list("foo");
    assertThat(listPresent.status).isEqualTo(YamlObject.Status.PRESENT);
    assertThat(listPresent.items.stream()
      .map(tree -> ((ScalarTree) tree).value()))
        .containsExactly("bar", "car");

    ListObject listAbsent = block.list("bar");
    assertThat(listAbsent.items).isEmpty();
    assertThat(listAbsent.status).isEqualTo(YamlObject.Status.ABSENT);
  }

  public MappingTree parseMap(String source) {
    return parse(source, MappingTree.class);
  }
}
