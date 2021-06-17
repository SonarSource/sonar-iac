/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.extension.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;

public class TreeVisitor<C extends TreeContext> {

  private final List<ConsumerFilter<C, ?>> consumers = new ArrayList<>();

  public void scan(C ctx, @Nullable Tree root) {
    if (root != null) {
      ctx.before();
      before(ctx, root);
      visit(ctx, root);
      after(ctx, root);
    }
  }

  private void visit(C ctx, @Nullable Tree node) {
    if (node != null) {
      ctx.enter(node);
      for (ConsumerFilter<C, ?> consumer : consumers) {
        consumer.accept(ctx, node);
      }
      node.children().forEach(child -> visit(ctx, child));
      ctx.leave();
    }
  }

  protected void before(C ctx, Tree root) {
    // default behaviour is to do nothing
  }

  protected void after(C ctx, Tree root) {
    // default behaviour is to do nothing
  }

  public <T extends Tree> TreeVisitor<C> register(Class<T> cls, BiConsumer<C, T> visitor) {
    consumers.add(new ConsumerFilter<>(cls, visitor));
    return this;
  }

  private static class ConsumerFilter<C extends TreeContext, T extends Tree> {

    private final Class<T> cls;

    private final BiConsumer<C, T> delegate;

    private ConsumerFilter(Class<T> cls, BiConsumer<C, T> delegate) {
      this.cls = cls;
      this.delegate = delegate;
    }

    private void accept(C ctx, Tree node) {
      if (cls.isAssignableFrom(node.getClass())) {
        delegate.accept(ctx, cls.cast(node));
      }
    }
  }
}
