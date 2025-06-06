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
package org.sonar.iac.terraform.symbols;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.checkdsl.ContextualTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

public class BlockSymbol extends ContextualTree<BlockSymbol, BlockTree> {

  protected BlockSymbol(CheckContext ctx, @Nullable BlockTree tree, String name, @Nullable BlockSymbol parent) {
    super(ctx, tree, name, parent);
  }

  public static BlockSymbol fromPresent(CheckContext ctx, BlockTree tree, @Nullable BlockSymbol parent) {
    return new BlockSymbol(ctx, tree, tree.key().value(), parent);
  }

  public static BlockSymbol fromAbsent(CheckContext ctx, String name, @Nullable BlockSymbol parent) {
    return new BlockSymbol(ctx, null, name, parent);
  }

  public BlockSymbol block(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, name, BlockTree.class))
      .map(block -> BlockSymbol.fromPresent(ctx, block, this))
      .orElse(BlockSymbol.fromAbsent(ctx, name, this));
  }

  public Stream<BlockSymbol> blocks(String name) {
    return PropertyUtils.getAll(tree, name, BlockTree.class).stream()
      .map(block -> BlockSymbol.fromPresent(ctx, block, this));
  }

  public ListSymbol list(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, name, AttributeTree.class))
      .map(attribute -> ListSymbol.fromPresent(ctx, attribute, this))
      .orElse(ListSymbol.fromAbsent(ctx, name, this));
  }

  public AttributeSymbol attribute(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, name, AttributeTree.class))
      .map(attribute -> AttributeSymbol.fromPresent(ctx, attribute, this))
      .orElse(AttributeSymbol.fromAbsent(ctx, name, this));
  }

  public ReferenceSymbol reference(String name) {
    return Optional.ofNullable(tree)
      .flatMap(tree -> PropertyUtils.get(tree, name, AttributeTree.class))
      .map(attribute -> ReferenceSymbol.fromPresent(ctx, attribute, this))
      .orElse(ReferenceSymbol.fromAbsent(ctx, name, this));
  }

  public BlockSymbol consume(Consumer<BlockSymbol> consumer) {
    consumer.accept(this);
    return this;
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree != null ? tree.key() : null;
  }
}
