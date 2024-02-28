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
package org.sonar.iac.common.yaml.object;

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class BlockObject extends YamlObject<BlockObject, MappingTree> {

  protected BlockObject(CheckContext ctx, @Nullable MappingTree tree, String key, Status status) {
    super(ctx, tree, key, status);
  }

  @Nullable
  @Override
  protected TextRange toHighlight() {
    if (tree != null) {
      return tree.toHighlight();
    }
    return null;
  }

  public static BlockObject fromPresent(CheckContext ctx, YamlTree tree, String key) {
    if (tree instanceof MappingTree mappingTree) {
      return new BlockObject(ctx, mappingTree, key, Status.PRESENT);
    }
    return new BlockObject(ctx, null, key, Status.UNKNOWN);
  }

  public static BlockObject fromAbsent(CheckContext ctx, String key) {
    return new BlockObject(ctx, null, key, Status.ABSENT);
  }

  public Stream<BlockObject> blocks(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.value(tree, key, SequenceTree.class))
      .map(sequence -> sequence.elements().stream()
        .map(block -> BlockObject.fromPresent(ctx, block, key)))
      .orElse(Stream.empty());
  }

  public BlockObject block(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, key, TupleTree.class))
      .map(tuple -> BlockObject.fromPresent(ctx, tuple.value(), key))
      .orElse(BlockObject.fromAbsent(ctx, key));
  }

  public AttributeObject attribute(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, key, TupleTree.class))
      .map(attribute -> AttributeObject.fromPresent(ctx, attribute, key))
      .orElse(AttributeObject.fromAbsent(ctx, key));
  }

  public ListObject list(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, key, TupleTree.class))
      .map(attribute -> ListObject.fromPresent(ctx, attribute, key, null))
      .orElse(ListObject.fromAbsent(ctx, key));
  }
}
