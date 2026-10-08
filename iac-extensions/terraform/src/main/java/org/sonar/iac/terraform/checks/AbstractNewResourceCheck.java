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
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

public abstract class AbstractNewResourceCheck implements IacCheck {

  private final Map<String, List<Consumer<ResourceSymbol>>> resourceConsumers = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, this::provideResource);
    registerResourceConsumer();
  }

  protected abstract void registerResourceConsumer();

  protected void provideResource(CheckContext ctx, BlockTree blockTree) {
    if (isResourceOrEphemeral(blockTree)) {
      ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, blockTree);
      if (resourceConsumers.containsKey(resource.type)) {
        resourceConsumers.get(resource.type).forEach(consumer -> consumer.accept(resource));
      }
    }
  }

  protected void register(String resourceName, Consumer<ResourceSymbol> consumer) {
    resourceConsumers.computeIfAbsent(resourceName, i -> new ArrayList<>()).add(consumer);
  }

  protected void register(Collection<String> resourceNames, Consumer<ResourceSymbol> consumer) {
    resourceNames.forEach(resourceName -> register(resourceName, consumer));
  }

  /** Only matches persisted resource declarations. */
  public static boolean isResource(BlockTree blockTree) {
    return "resource".equals(blockTree.key().value());
  }

  /** Resource consumers also receive ephemeral declarations of the same type, although their provider arguments may differ. */
  public static boolean isResourceOrEphemeral(BlockTree blockTree) {
    return isResource(blockTree) || "ephemeral".equals(blockTree.key().value());
  }

  /**
   * Top-level blocks followed by the scoped data blocks of check blocks, the only blocks Terraform allows there besides assert.
   * Top-level blocks come first, so they take precedence over check-scoped data blocks with the same name when building indexes.
   */
  protected static Stream<BlockTree> blocksIncludingCheckChildren(FileTree tree) {
    List<BlockTree> topLevelBlocks = tree.properties().stream()
      .filter(BlockTree.class::isInstance)
      .map(BlockTree.class::cast)
      .toList();
    Stream<BlockTree> checkScopedData = topLevelBlocks.stream()
      .filter(block -> "check".equals(block.key().value()))
      .flatMap(check -> check.properties().stream()
        .filter(BlockTree.class::isInstance)
        .map(BlockTree.class::cast)
        .filter(block -> "data".equals(block.key().value())));
    return Stream.concat(topLevelBlocks.stream(), checkScopedData);
  }

  /** Matches a data source declaration by type. */
  public static boolean isDataOfType(BlockTree blockTree, String dataType) {
    return "data".equals(blockTree.key().value()) && dataType.equals(resourceType(blockTree));
  }

  /** Despite its name, this method works fine for 'resource', 'data' and all other sorts of Terraform top-level blocks */
  @Nullable
  public static String resourceType(BlockTree tree) {
    return tree.labels().isEmpty() ? null : tree.labels().get(0).value();
  }
}
