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
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.getResourceType;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;

public abstract class ResourceProvider implements IacCheck {

  private static final Map<String, List<Consumer<Resource>>> resourceConsumers = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, this::provideResource);
    registerResourceConsumer();
  }

  protected abstract void registerResourceConsumer();

  protected void provideResource(CheckContext ctx, BlockTree blockTree) {
    if (isResource(blockTree)) {
      Resource resource = new Resource(ctx, blockTree);
      if (resourceConsumers.containsKey(resource.type)) {
        resourceConsumers.get(resource.type).forEach(consumer -> consumer.accept(resource));
      }
    }
  }

  protected void addConsumer(String resourceName, Consumer<Resource> consumer) {
    resourceConsumers.computeIfAbsent(resourceName, i -> new ArrayList<>()).add(consumer);
  }

  protected void addConsumer(List<String> resourceNames, Consumer<Resource> consumer) {
    resourceNames.forEach(resourceName -> addConsumer(resourceName, consumer));
  }

  protected static class Block {

    protected final CheckContext ctx;
    protected final BlockTree blockTree;
    protected final String key;
    private Block(CheckContext ctx, BlockTree blockTree) {
      this.ctx = ctx;
      this.blockTree = blockTree;
      this.key = blockTree.key().value();
    }

    public Attribute attribute(String propertyName) {
      return PropertyUtils.get(blockTree, propertyName, AttributeTree.class)
        .map(a -> (Attribute) new Attribute.PresentAttribute(ctx, this, propertyName, a))
        .orElse(new Attribute.AbsentAttribute(ctx, this, propertyName));
    }

    public Optional<Block> block(String propertyName) {
      return PropertyUtils.get(blockTree, propertyName, BlockTree.class).map(b -> new Block(ctx, b));
    }

    public Stream<Block> blocks(String propertyName) {
      return PropertyUtils.getAll(blockTree, propertyName, BlockTree.class).stream().map(b -> new Block(ctx, b));
    }

    public void report(String message, SecondaryLocation... secondaries) {
      ctx.reportIssue(blockTree.key(), message, Arrays.asList(secondaries));
    }
  }

  protected static class Resource extends Block  {
    private final String type;

    public Resource(CheckContext ctx, BlockTree resourceTree) {
      super(ctx, resourceTree);
      this.type = getResourceType(resourceTree);
    }

    @Override
    public void report(String message, SecondaryLocation... secondaries) {
      ctx.reportIssue(blockTree.labels().get(0), message, Arrays.asList(secondaries));
    }
  }

  public static class Attribute {

    protected final CheckContext ctx;
    protected final Block block;
    protected final String name;
    @Nullable
    protected final AttributeTree attributeTree;

    public Attribute(CheckContext ctx, Block block, String name, @Nullable AttributeTree attributeTree) {
      this.ctx = ctx;
      this.block = block;
      this.name = name;
      this.attributeTree = attributeTree;
    }

    public Attribute reportOnTrue(String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportOnFalse(String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }
    public Attribute reportUnexpectedValue(String expectedValue, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportSensitiveValue(String sensitiveValue, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportUnexpectedValue(Predicate<ExpressionTree> expectedPredicate, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportSensitiveValue(Predicate<ExpressionTree> expectedPredicate, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportAbsence(String message) {
      // designed to be extended but noop in standard case
      return this;
    }

    static class AbsentAttribute extends Attribute {

      public AbsentAttribute(CheckContext ctx, Block block, String name) {
        super(ctx, block, name, null);
      }

      @Override
      public Attribute reportAbsence(String absenceMessage) {
        block.report(String.format(absenceMessage, name));
        return this;
      }
    }

    static class PresentAttribute extends Attribute {

      public PresentAttribute(CheckContext ctx, Block block, String name, AttributeTree attributeTree) {
        super(ctx, block, name, attributeTree);
      }

      @Override
      public Attribute reportOnTrue(String message, SecondaryLocation... secondaries) {
        return reportSensitiveValue(TextUtils::isValueTrue, message, secondaries);
      }

      @Override
      public Attribute reportOnFalse(String message, SecondaryLocation... secondaries) {
        return reportSensitiveValue(TextUtils::isValueFalse, message, secondaries);
      }

      @Override
      public Attribute reportUnexpectedValue(String expectedValue, String message, SecondaryLocation... secondaries) {
        return reportSensitiveValue(value -> TextUtils.isValue(value, expectedValue).isFalse(), message, secondaries);
      }

      @Override
      public Attribute reportUnexpectedValue(Predicate<ExpressionTree> expectedPredicate, String message, SecondaryLocation... secondaries) {
        return reportSensitiveValue(expectedPredicate.negate(), message, secondaries);
      }

      @Override
      public Attribute reportSensitiveValue(String sensitiveValue, String message, SecondaryLocation... secondaries) {
        return reportSensitiveValue(value -> TextUtils.isValue(value, sensitiveValue).isTrue(), message, secondaries);
      }

      @Override
      public Attribute reportSensitiveValue(Predicate<ExpressionTree> expectedPredicate, String message, SecondaryLocation... secondaries) {
        if (expectedPredicate.test(attributeTree.value())) {
          report(message, secondaries);
        }
        return this;
      }

      private void report(String message, SecondaryLocation... secondaries) {
        ctx.reportIssue(attributeTree, message, Arrays.asList(secondaries));
      }
    }
  }
}
