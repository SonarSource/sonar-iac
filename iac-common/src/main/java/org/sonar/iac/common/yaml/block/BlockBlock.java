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

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class BlockBlock extends YamlBlock<MappingTree> {

  protected BlockBlock(CheckContext ctx, @Nullable MappingTree tree, String key, Status status) {
    super(ctx, tree, key, status);
  }

  public static BlockBlock fromPresent(CheckContext ctx, YamlTree tree, String key) {
    if (tree instanceof MappingTree mappingTree) {
      return new BlockBlock(ctx, mappingTree, key, Status.PRESENT);
    }
    return new BlockBlock(ctx, null, key, Status.UNKNOWN);
  }

  public static BlockBlock fromAbsent(CheckContext ctx, String key) {
    return new BlockBlock(ctx, null, key, Status.ABSENT);
  }

  public Stream<BlockBlock> blocks(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.value(tree, key, SequenceTree.class))
      .map(sequence -> sequence.elements().stream()
        .map(block -> BlockBlock.fromPresent(ctx, block, key)))
      .orElse(Stream.empty());
  }

  public BlockBlock block(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, key, TupleTree.class))
      .map(tuple -> BlockBlock.fromPresent(ctx, tuple.value(), key))
      .orElse(BlockBlock.fromAbsent(ctx, key));
  }

  public AttributeBlock attribute(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, key, TupleTree.class))
      .map(attribute -> AttributeBlock.fromPresent(ctx, attribute, key))
      .orElse(AttributeBlock.fromAbsent(ctx, key));
  }

  public ListBlock list(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, key, TupleTree.class))
      .map(attribute -> ListBlock.fromPresent(ctx, attribute, key, null))
      .orElse(ListBlock.fromAbsent(ctx, key));
  }
}
