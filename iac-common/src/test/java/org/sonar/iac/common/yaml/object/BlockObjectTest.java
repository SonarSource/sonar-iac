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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.yaml.YamlTreeTest;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.yaml.object.YamlObjectAssertions.assertThat;

class BlockObjectTest extends YamlTreeTest {

  CheckContext ctx = mock(CheckContext.class);

  @Test
  void shouldVerifyFromPresent() {
    MappingTree tree = parseMap("a: b");
    BlockObject block = BlockObject.fromPresent(ctx, tree, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block).isPresent();
    assertThat(block.tree).isEqualTo(tree);
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void shouldVerifyFromPresentUnknown() {
    YamlTree tree = parse("a:b", YamlTree.class);
    BlockObject block = BlockObject.fromPresent(ctx, tree, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block).isUnknown();
    assertThat(block.tree).isNull();
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void shouldVerifyFromAbsent() {
    BlockObject block = BlockObject.fromAbsent(ctx, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block).isAbsent();
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
    assertThat(presentBlock).isPresent();

    BlockObject absentBlock = block.block("bar");
    assertThat(absentBlock).isAbsent();
  }

  @Test
  void shouldVerifyAttribute() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo: bar"), "a");
    AttributeObject presentAttr = block.attribute("foo");
    assertThat(presentAttr).isPresent();

    AttributeObject absentAttr = block.attribute("bar");
    assertThat(absentAttr).isAbsent();
  }

  @Test
  void shouldVerifyAttributes() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo: bar"), "a");
    var attrs = block.attributes(s -> s.contains("o")).toList();
    assertThat(attrs).hasSize(1);
    AttributeObject presentAttr = attrs.get(0);
    assertThat(presentAttr).isPresent();

    var noAttrs = block.attributes(s -> false).toList();
    assertThat(noAttrs).isEmpty();
  }

  @Test
  void shouldVerifyList() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo: [bar, car]"), "a");
    ListObject listPresent = block.list("foo");
    assertThat(listPresent).isPresent();
    assertThat(listPresent.items.stream()
      .map(tree -> ((ScalarTree) tree).value()))
        .containsExactly("bar", "car");

    ListObject listAbsent = block.list("bar");
    assertThat(listAbsent.items).isEmpty();
    assertThat(listAbsent).isAbsent();
  }

  @ParameterizedTest
  @ValueSource(strings = {"foo: [bar, car]",
    """
      foo:
        - bar
        - car
      """})
  void shouldVerifyLists(String code) {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap(code), "anything");
    List<ListObject> lists = block.lists("foo"::equals).toList();
    assertThat(lists).hasSize(1);
    var listPresent = lists.get(0);
    assertThat(listPresent.items.stream()
      .map(tree -> ((ScalarTree) tree).value()))
        .containsExactly("bar", "car");

    ListObject listAbsent = block.list("bar");
    assertThat(listAbsent.items).isEmpty();
    assertThat(listAbsent).isAbsent();
  }

  @Test
  void shouldVerifyListsEmpty() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo: [bar, car]"), "anything");
    List<ListObject> lists = block.lists("bob"::equals).toList();
    assertThat(lists).isEmpty();
  }

  @Test
  void shouldVerifyListsMultipleResult() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("""
      foo1: [bar, car]
      foo2: [tar, jar]
      """), "anything");
    List<ListObject> lists = block.lists(s -> s.startsWith("foo")).toList();
    assertThat(lists).hasSize(2);
    var list1 = lists.get(0);
    assertThat(list1.items.stream()
      .map(tree -> ((ScalarTree) tree).value()))
        .containsExactly("bar", "car");
    var list2 = lists.get(1);
    assertThat(list2.items.stream()
      .map(tree -> ((ScalarTree) tree).value()))
        .containsExactly("tar", "jar");
  }

  public MappingTree parseMap(String source) {
    return parse(source, MappingTree.class);
  }
}
