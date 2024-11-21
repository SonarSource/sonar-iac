/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.jvmframeworkconfig.parser.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.yaml.IacYamlConverter;
import org.sonar.iac.common.yaml.YamlConverter;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;
import org.sonar.iac.jvmframeworkconfig.tree.api.File;
import org.sonar.iac.jvmframeworkconfig.tree.api.Profile;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;
import org.sonar.iac.jvmframeworkconfig.tree.impl.FileImpl;
import org.sonar.iac.jvmframeworkconfig.tree.impl.ProfileImpl;
import org.sonar.iac.jvmframeworkconfig.tree.impl.ScalarImpl;
import org.sonar.iac.jvmframeworkconfig.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.jvmframeworkconfig.tree.impl.TupleImpl;

import static org.sonar.iac.jvmframeworkconfig.parser.JvmFrameworkConfigProfileNameUtil.profileName;

public class JvmFrameworkConfigYamlConverter implements IacYamlConverter<File, Stream<JvmFrameworkConfigYamlConverter.TupleBuilder>> {
  private final YamlConverter yamlConverter = new YamlConverter();
  private List<Comment> commentsPerProfile = new ArrayList<>();

  @Override
  public File convertFile(List<Node> nodes) {
    if (nodes.isEmpty()) {
      throw new ParseException("Unexpected empty nodes list while converting file", null, null);
    }

    List<Profile> profiles = nodes.stream()
      .map(this::convertToProfile)
      .toList();

    // This step is needed for syntax highlighting later. Keeping track of YAML nodes in the leaves would be more complicated,
    // because in the flattened structure leaves would have a lot of common ancestors, and they would need to be filtered later.
    var yamlFileTree = yamlConverter.convertFile(nodes);
    return new FileImpl(profiles, yamlFileTree);
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
    var tupleBuilder = new TupleBuilder()
      .withValue(scalarNode.getValue())
      .withValueTextRange(range);
    return Stream.of(tupleBuilder);
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
    boolean valueIsEmptySequence = tuple.getValueNode() instanceof SequenceNode sequenceNode && sequenceNode.getValue().isEmpty();

    return convert(tuple.getValueNode())
      .map((TupleBuilder childTuple) -> {
        if (valueIsScalar || valueIsEmptySequence) {
          return childTuple.withKeyTextRange(YamlTreeMetadata.Builder.range(tuple.getKeyNode()));
        }
        return childTuple;
      })
      .map(childTuple -> childTuple.prefixKeyDelimited(keyPrefix, tuple.getValueNode().getClass()));
  }

  @Override
  public Stream<TupleBuilder> convertSequence(SequenceNode sequenceNode) {
    // If this is an empty sequence, it means it is a flow style empty array like 'key: []'. We return a single empty tupleBuilder so that we
    // keep a mapped null value to the key. Otherwise, it results in a Profile with no child, which throw an error when computing TextRange.
    if (sequenceNode.getValue().isEmpty()) {
      return Stream.of(new TupleBuilder());
    }
    return IntStream
      .range(0, sequenceNode.getValue().size())
      .mapToObj((int index) -> {
        var node = sequenceNode.getValue().get(index);
        var keyPrefix = "[" + index + "]";
        return convert(node)
          .map((TupleBuilder childTuple) -> childTuple.prefixKeyDelimited(keyPrefix, node.getClass()));
      })
      .flatMap(Function.identity());
  }

  public static class TupleBuilder {
    private final List<String> keysReversed = new ArrayList<>();
    private TextRange keyTextRange;
    @Nullable
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
      var keyToken = new SyntaxTokenImpl(buildKey(keysReversed), keyTextRange);
      var valueToken = Optional.ofNullable(value)
        .map(val -> new SyntaxTokenImpl(val, valueTextRange))
        .map(ScalarImpl::new)
        .orElse(null);
      return new TupleImpl(new ScalarImpl(keyToken), valueToken);
    }

    private static String buildKey(List<String> keysReversed) {
      var sb = new StringBuilder();

      for (int i = keysReversed.size() - 1; i >= 0; i--) {
        sb.append(keysReversed.get(i));
      }

      return sb.toString();
    }

    public TupleBuilder prefixKeyDelimited(String prefix, Class<? extends Node> followingNodeType) {
      // In case the following node type is a scalar, the key is empty and we don't need the delimiter
      if (followingNodeType != ScalarNode.class && followingNodeType != SequenceNode.class) {
        keysReversed.add(".");
      }
      keysReversed.add(prefix);
      return this;
    }
  }
}
