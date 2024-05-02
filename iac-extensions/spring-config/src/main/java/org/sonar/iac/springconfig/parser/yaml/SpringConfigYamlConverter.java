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
package org.sonar.iac.springconfig.parser.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.yaml.IacYamlConverter;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;
import org.sonar.iac.springconfig.tree.api.File;
import org.sonar.iac.springconfig.tree.api.Profile;
import org.sonar.iac.springconfig.tree.api.Tuple;
import org.sonar.iac.springconfig.tree.impl.FileImpl;
import org.sonar.iac.springconfig.tree.impl.ProfileImpl;
import org.sonar.iac.springconfig.tree.impl.ScalarImpl;
import org.sonar.iac.springconfig.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.springconfig.tree.impl.TupleImpl;

import static org.sonar.iac.springconfig.parser.SpringConfigProfileNameUtil.profileName;

public class SpringConfigYamlConverter implements IacYamlConverter<File, Stream<SpringConfigYamlConverter.TupleBuilder>> {

  private List<Comment> commentsPerProfile = new ArrayList<>();

  @Override
  public File convertFile(List<Node> nodes) {
    if (nodes.isEmpty()) {
      throw new ParseException("Unexpected empty nodes list while converting file", null, null);
    }

    List<Profile> profiles = nodes.stream()
      .map(this::convertToProfile)
      .toList();

    return new FileImpl(profiles);
  }

  private Profile convertToProfile(Node node) {
    commentsPerProfile = new ArrayList<>();
    List<Tuple> tuples = convert(node).map(TupleBuilder::build).toList();

    return new ProfileImpl(tuples, commentsPerProfile, profileName(tuples), true);
  }

  @Override
  public Stream<TupleBuilder> convert(Node node) {
    addComments(node);
    return IacYamlConverter.super.convert(node);
  }

  private void addComments(Node node) {
    commentsPerProfile.addAll(YamlTreeMetadata.Builder.comments(node));
  }

  @Override
  public Stream<TupleBuilder> convertMapping(MappingNode mappingNode) {
    return mappingNode.getValue().stream().flatMap(this::convertTuple);
  }

  @Override
  public Stream<TupleBuilder> convertScalar(ScalarNode scalarNode) {
    TextRange range = YamlTreeMetadata.Builder.range(scalarNode);
    var tuple = new TupleBuilder()
      .withValue(scalarNode.getValue())
      .withValueTextRange(range);
    return Stream.of(tuple);
  }

  @Override
  public Stream<TupleBuilder> convertTuple(NodeTuple tuple) {
    if (!(tuple.getKeyNode() instanceof ScalarNode)) {
      throw new ParseException("Unexpected non-scalar key node in tuple", null, null);
    }

    // comments for the valueNode are handled during the convert call
    addComments(tuple.getKeyNode());

    String keyPrefix = ((ScalarNode) tuple.getKeyNode()).getValue();
    boolean valueIsScalar = tuple.getValueNode() instanceof ScalarNode;

    return convert(tuple.getValueNode())
      .map((TupleBuilder childTuple) -> {
        if (valueIsScalar) {
          return childTuple.withKeyTextRange(YamlTreeMetadata.Builder.range(tuple.getKeyNode()));
        }
        return childTuple;
      })
      .map(childTuple -> childTuple.prefixKeyDelimited(keyPrefix, tuple.getValueNode().getClass()));
  }

  @Override
  public Stream<TupleBuilder> convertSequence(SequenceNode sequenceNode) {
    return IntStream
      .range(0, sequenceNode.getValue().size())
      .mapToObj(index -> {
        var node = sequenceNode.getValue().get(index);
        var keyPrefix = "[%d]".formatted(index);
        return convert(node)
          .map((TupleBuilder childTuple) -> childTuple.prefixKeyDelimited(keyPrefix, node.getClass()));
      })
      .flatMap(Function.identity());
  }

  public static class TupleBuilder {
    private String key = "";
    private TextRange keyTextRange;
    private String value;
    private TextRange valueTextRange;

    public TupleBuilder withKeyTextRange(TextRange keyTextRange) {
      this.keyTextRange = keyTextRange;
      return this;
    }

    public TupleBuilder withValue(String value) {
      this.value = value;
      return this;
    }

    public TupleBuilder withValueTextRange(TextRange valueTextRange) {
      this.valueTextRange = valueTextRange;
      return this;
    }

    public Tuple build() {
      if (keyTextRange == null) {
        // in case the value is a scalar and direct child of a sequence, the keyTextRange is not set
        // in this case, we set the keyTextRange the same as the valueTextRange
        keyTextRange = valueTextRange;
      }
      var keyToken = new SyntaxTokenImpl(key, keyTextRange);
      var valueToken = new SyntaxTokenImpl(value, valueTextRange);
      return new TupleImpl(new ScalarImpl(keyToken), new ScalarImpl(valueToken));
    }

    public TupleBuilder prefixKeyDelimited(String prefix, Class<? extends Node> followingNodeType) {
      var formatString = "%s.%s";
      if (followingNodeType == ScalarNode.class || followingNodeType == SequenceNode.class) {
        // In case the following node type is a scalar, the key is empty and we don't need the delimiter
        formatString = "%s%s";
      }
      key = formatString.formatted(prefix, key);
      return this;
    }
  }
}
