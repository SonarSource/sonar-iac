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
package org.sonar.iac.common.extension.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;

public class TreeVisitor<C extends TreeContext> {

  protected final List<ConsumerFilter<C, ?>> consumers = new ArrayList<>();

  public void scan(C ctx, @Nullable Tree root) {
    if (root != null) {
      ctx.before();
      before(ctx, root);
      visit(ctx, root);
      after(ctx, root);
    }
  }

  protected void visit(C ctx, @Nullable Tree node) {
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

  public final <T extends Tree> TreeVisitor<C> register(Class<T> cls, BiConsumer<C, T> visitor) {
    consumers.add(new ConsumerFilter<>(cls, visitor));
    return this;
  }

  protected static class ConsumerFilter<C extends TreeContext, T extends Tree> {

    private final Class<T> cls;

    private final BiConsumer<C, T> delegate;

    public ConsumerFilter(Class<T> cls, BiConsumer<C, T> delegate) {
      this.cls = cls;
      this.delegate = delegate;
    }

    public void accept(C ctx, Tree node) {
      if (cls.isAssignableFrom(node.getClass())) {
        delegate.accept(ctx, cls.cast(node));
      }
    }
  }
}
