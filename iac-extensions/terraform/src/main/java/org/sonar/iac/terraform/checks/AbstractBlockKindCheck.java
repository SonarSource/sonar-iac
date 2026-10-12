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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.symbols.BlockSymbol;

/** Base class for checks on top-level declarations other than resources; unlike resources, these blocks do not necessarily have two labels. */
public abstract class AbstractBlockKindCheck implements IacCheck {

  public enum BlockKind {
    VARIABLE("variable"),
    LOCALS("locals"),
    MODULE("module"),
    PROVIDER("provider"),
    OUTPUT("output"),
    TERRAFORM("terraform");

    private final String key;

    BlockKind(String key) {
      this.key = key;
    }
  }

  private final Map<String, List<Consumer<BlockSymbol>>> blockConsumers = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, (ctx, tree) -> {
      if (tree.isTopLevel()) {
        var consumers = blockConsumers.get(tree.key().value());
        if (consumers != null) {
          var block = BlockSymbol.fromPresent(ctx, tree, null);
          consumers.forEach(consumer -> consumer.accept(block));
        }
      }
    });
    registerBlockConsumers();
  }

  protected abstract void registerBlockConsumers();

  /** Registers a consumer for a declaration kind; nested blocks are accessed through the supplied symbol. */
  protected void register(BlockKind kind, Consumer<BlockSymbol> consumer) {
    blockConsumers.computeIfAbsent(kind.key, key -> new ArrayList<>()).add(consumer);
  }
}
