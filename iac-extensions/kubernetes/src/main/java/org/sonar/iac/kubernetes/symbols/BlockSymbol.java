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
package org.sonar.iac.kubernetes.symbols;

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

abstract class BlockSymbol<T extends KubernetesSymbol<?, ?>> extends KubernetesSymbol<T, MappingTree> {

  protected BlockSymbol(CheckContext ctx, @Nullable MappingTree tree, String key, @Nullable BlockSymbol<?> parent) {
    super(ctx, tree, key, parent);
  }

  public Stream<SequenceBlockSymbol> blocks(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.value(tree, key, SequenceTree.class))
      .map(sequence -> sequence.elements().stream()
          .map(block -> SequenceBlockSymbol.fromPresent(ctx, block, key, this)))
      .orElse(Stream.empty());
  }

  public TupleBlockSymbol block(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, key, TupleTree.class))
      .map(tuple -> TupleBlockSymbol.fromPresent(ctx, tuple, key, this))
      .orElse(TupleBlockSymbol.fromAbsent(ctx, key, this));
  }

  public TupleSymbol attribute(String key) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, key, TupleTree.class))
      .map(attribute -> TupleSymbol.fromPresent(ctx, attribute, key, this))
      .orElse(TupleSymbol.fromAbsent(ctx, key, this));
  }
}
