/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.jspecify.annotations.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.checkdsl.ContextualTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
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

  /** Whether this top-level declaration produces a value that Terraform does not persist in state. */
  public boolean isEphemeral() {
    if (tree == null || !tree.isTopLevel() || tree.isDynamic()) {
      return false;
    }
    return switch (tree.key().value()) {
      case "ephemeral" -> tree.labels().size() == 2;
      case "variable", "output" -> tree.labels().size() == 1 && hasEphemeralFlagSet(tree);
      default -> false;
    };
  }

  private static boolean hasEphemeralFlagSet(BlockTree tree) {
    return PropertyUtils.get(tree, "ephemeral", AttributeTree.class)
      .map(attribute -> TextUtils.isValueTrue(attribute.value()))
      .orElse(false);
  }

  /** Whether this is a store block directly inside a terraform_data resource. */
  public boolean isSensitiveStore() {
    return tree != null
      && "store".equals(tree.key().value())
      && parent instanceof ResourceSymbol resource
      && resource.isResourceDeclaration()
      && "terraform_data".equals(resource.type);
  }

  /** Whether this block is nested inside a sensitive terraform_data store block. */
  public boolean isSensitiveStoreContent() {
    return tree != null && parent instanceof BlockSymbol block && (block.isSensitiveStore() || block.isSensitiveStoreContent());
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
