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
import java.util.regex.Pattern;
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
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.TupleTree;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.getResourceType;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;

public abstract class ResourceVisitor implements IacCheck {

  private final Map<String, List<Consumer<Resource>>> resourceConsumers = new HashMap<>();

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

  protected void register(String resourceName, Consumer<Resource> consumer) {
    resourceConsumers.computeIfAbsent(resourceName, i -> new ArrayList<>()).add(consumer);
  }

  protected void register(List<String> resourceNames, Consumer<Resource> consumer) {
    resourceNames.forEach(resourceName -> register(resourceName, consumer));
  }

  protected static class Block {

    protected final CheckContext ctx;
    protected final BlockTree blockTree;

    public Block(CheckContext ctx, BlockTree blockTree) {
      this.ctx = ctx;
      this.blockTree = blockTree;
    }

    public CheckContext context() {
      return ctx;
    }

    public Attribute attribute(String propertyName) {
      return PropertyUtils.get(blockTree, propertyName, AttributeTree.class)
        .map(a -> (Attribute) new Attribute.PresentAttribute(ctx, this, propertyName, a))
        .orElse(new Attribute.AbsentAttribute(ctx, this, propertyName));
    }

    public ListProperty list(String propertyName) {
      return PropertyUtils.get(blockTree, propertyName, AttributeTree.class)
        .filter(attr -> attr.value() instanceof TupleTree)
        .map(attr -> (ListProperty) new ListProperty.PresentListProperty(ctx, this, propertyName, attr))
        .orElse(new ListProperty.AbsentListProperty(ctx, this, propertyName));
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

  public abstract static class Property {
    protected final CheckContext ctx;
    protected final Block block;
    protected final String name;

    Property(CheckContext ctx, Block block, String name) {
      this.ctx = ctx;
      this.block = block;
      this.name = name;
    }
  }

  public abstract static class Attribute extends Property {

    @Nullable
    protected final AttributeTree attributeTree;

    Attribute(CheckContext ctx, Block block, String name, @Nullable AttributeTree attributeTree) {
      super(ctx, block, name);
      this.attributeTree = attributeTree;
    }

    public void value(Consumer<ExpressionTree> consumer) {
      // designed to be extended but noop in standard case
    }

    public Attribute reportIfTrue(String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportIfFalse(String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }
    public Attribute reportIfValueDoesNotMatch(String expectedValue, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportIfValueMatches(String expectedValue, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportIfValueDoesNotMatch(Predicate<ExpressionTree> expectedPredicate, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportIfValueMatches(Pattern expectedValuePattern, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportIfValueContains(Pattern expectedValueSubPattern, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportIfValueMatches(Predicate<ExpressionTree> expectedPredicate, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
      return this;
    }

    public Attribute reportIfAbsence(String message) {
      // designed to be extended but noop in standard case
      return this;
    }

    static class AbsentAttribute extends Attribute {

      public AbsentAttribute(CheckContext ctx, Block block, String name) {
        super(ctx, block, name, null);
      }

      @Override
      public Attribute reportIfAbsence(String absenceMessage) {
        block.report(String.format(absenceMessage, name));
        return this;
      }
    }

    static class PresentAttribute extends Attribute {

      public PresentAttribute(CheckContext ctx, Block block, String name, AttributeTree attributeTree) {
        super(ctx, block, name, attributeTree);
      }

      @Override
      public void value(Consumer<ExpressionTree> consumer) {
        consumer.accept(attributeTree.value());
      }

      @Override
      public Attribute reportIfTrue(String message, SecondaryLocation... secondaries) {
        return reportIfValueMatches(TextUtils::isValueTrue, message, secondaries);
      }

      @Override
      public Attribute reportIfFalse(String message, SecondaryLocation... secondaries) {
        return reportIfValueMatches(TextUtils::isValueFalse, message, secondaries);
      }

      @Override
      public Attribute reportIfValueDoesNotMatch(String expectedValue, String message, SecondaryLocation... secondaries) {
        return reportIfValueMatches(value -> TextUtils.isValue(value, expectedValue).isFalse(), message, secondaries);
      }

      @Override
      public Attribute reportIfValueDoesNotMatch(Predicate<ExpressionTree> expectedPredicate, String message, SecondaryLocation... secondaries) {
        return reportIfValueMatches(expectedPredicate.negate(), message, secondaries);
      }

      @Override
      public Attribute reportIfValueMatches(String expectedValue, String message, SecondaryLocation... secondaries) {
        return reportIfValueMatches(value -> TextUtils.isValue(value, expectedValue).isTrue(), message, secondaries);
      }

      @Override
      public Attribute reportIfValueMatches(Pattern expectedValuePattern, String message, SecondaryLocation... secondaries) {
        return reportIfValueMatches(tree -> TextUtils.matchesValue(tree, value -> expectedValuePattern.matcher(value).matches()).isTrue(), message, secondaries);
      }

      @Override
      public Attribute reportIfValueContains(Pattern expectedValueSubPattern, String message, SecondaryLocation... secondaries) {
        return reportIfValueMatches(tree -> TextUtils.matchesValue(tree, value -> expectedValueSubPattern.matcher(value).find()).isTrue(), message, secondaries);
      }

      @Override
      public Attribute reportIfValueMatches(Predicate<ExpressionTree> expectedPredicate, String message, SecondaryLocation... secondaries) {
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

  public abstract static class ListProperty extends Property {

    private ListProperty(CheckContext ctx, Block block, String name) {
      super(ctx, block, name);
    }

    public void reportItemsWhichMatch(Predicate<ExpressionTree> predicate, String message, SecondaryLocation... secondaries) {
      // designed to be extended but noop in standard case
    }

    static class PresentListProperty extends ListProperty {

      private List<ExpressionTree> items;

      public PresentListProperty(CheckContext ctx, Block block, String name, AttributeTree attributeTree) {
        super(ctx, block, name);
        if (!attributeTree.value().is(TerraformTree.Kind.TUPLE)) {
          throw new IllegalArgumentException("ListProperty is created on an AttributeTree which does not contain a TupleTree");
        }
        this.items = ((TupleTree) attributeTree.value()).elements().trees();
      }
      
      @Override
      public void reportItemsWhichMatch(Predicate<ExpressionTree> predicate, String message, SecondaryLocation... secondaries) {
        items.stream().filter(predicate).forEach(item -> ctx.reportIssue(item, message, Arrays.asList(secondaries)));
      }
    }

    static class AbsentListProperty extends ListProperty {

      public AbsentListProperty(CheckContext ctx, Block block, String name) {
        super(ctx, block, name);
      }
    }
  }
}
