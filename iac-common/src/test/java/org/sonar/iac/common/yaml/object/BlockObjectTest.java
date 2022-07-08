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

  private MappingTree parseMap(String source) {
    return (MappingTree) PARSER.parse(source, null).root();
  }

}
