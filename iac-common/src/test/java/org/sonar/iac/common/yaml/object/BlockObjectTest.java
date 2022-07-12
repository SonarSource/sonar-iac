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
package org.sonar.iac.common.yaml.object;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BlockObjectTest {

  static YamlParser PARSER = new YamlParser();
  CheckContext ctx = mock(CheckContext.class);

  @Test
  void fromPresent() {
    MappingTree tree = parseMap("a: b");
    BlockObject block = BlockObject.fromPresent(ctx, tree, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block.status).isEqualTo(YamlObject.Status.PRESENT);
    assertThat(block.tree).isEqualTo(tree);
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void fromPresent_unknown() {
    YamlTree tree = PARSER.parse("a:b", null).root();
    BlockObject block = BlockObject.fromPresent(ctx, tree, "a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block.status).isEqualTo(YamlObject.Status.UNKNOWN);
    assertThat(block.tree).isNull();
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void fromAbsent() {
    BlockObject block = BlockObject.fromAbsent(ctx,"a");
    assertThat(block.key).isEqualTo("a");
    assertThat(block.status).isEqualTo(YamlObject.Status.ABSENT);
    assertThat(block.tree).isNull();
    assertThat(block.ctx).isEqualTo(ctx);
  }

  @Test
  void blocks() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo:\n - key: value"), "a");
    List<BlockObject> presentBlocks = block.blocks("foo").collect(Collectors.toList());

    assertThat(presentBlocks).hasSize(1);
    assertThat(presentBlocks.get(0).key).isEqualTo("foo");
  }

  @Test
  void block() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo:\n key: value"), "a");
    BlockObject presentBlock = block.block("foo");
    assertThat(presentBlock.status).isEqualTo(YamlObject.Status.PRESENT);

    BlockObject absentBlock = block.block("bar");
    assertThat(absentBlock.status).isEqualTo(YamlObject.Status.ABSENT);
  }

  @Test
  void attribute() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo: bar"), "a");
    AttributeObject presentAttr = block.attribute("foo");
    assertThat(presentAttr.status).isEqualTo(YamlObject.Status.PRESENT);

    AttributeObject absentAttr = block.attribute("bar");
    assertThat(absentAttr.status).isEqualTo(YamlObject.Status.ABSENT);
  }

  @Test
  void attributes() {
    BlockObject block = BlockObject.fromPresent(ctx, parseMap("foo: bar \nfoo2: bar2 \nfoo3: bar3"), "a");
    List<AttributeObject> attributes = block.attributes(List.of("foo", "foo3"));

    assertThat(attributes).hasSize(2);
    assertThat(attributes.stream().map(attribute -> attribute.status)).containsOnly(YamlObject.Status.PRESENT);
    assertThat(attributes).doesNotContain(block.attribute("foo2"));

    AttributeObject absentAttr = block.attribute("bar");
    assertThat(absentAttr.status).isEqualTo(YamlObject.Status.ABSENT);

    BlockObject block2 = BlockObject.fromPresent(ctx, parseMap("foo: bar"), "a");
    List<AttributeObject> attributes2 = block2.attributes(List.of("foo2", "foo3"));
    assertThat(attributes2.stream().map(attribute -> attribute.status)).containsOnly(YamlObject.Status.ABSENT);
  }

  private MappingTree parseMap(String source) {
    return (MappingTree) PARSER.parse(source, null).root();
  }

}
