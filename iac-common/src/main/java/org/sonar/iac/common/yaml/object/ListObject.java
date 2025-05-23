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
package org.sonar.iac.common.yaml.object;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class ListObject extends YamlObject<SequenceTree> {

  protected final List<YamlTree> items;

  @Nullable
  private final YamlTree parent;

  ListObject(CheckContext ctx, @Nullable SequenceTree tree, @Nullable String key, Status status, @Nullable YamlTree parent, List<YamlTree> items) {
    super(ctx, tree, key, status);
    this.parent = parent;
    this.items = items;
  }

  public static ListObject fromPresent(CheckContext ctx, YamlTree tree, @Nullable String key, @Nullable YamlTree parent) {
    if (tree instanceof TupleTree tupleTree) {
      return fromPresent(ctx, tupleTree.value(), key, tree);
    }
    if (tree instanceof SequenceTree sequenceTree) {
      return new ListObject(ctx, sequenceTree, key, Status.PRESENT, parent, sequenceTree.elements());
    }
    // List can also be provided as reference. To avoid false positives due to a missing reference resolution
    // we create an empty ListObject
    return new ListObject(ctx, null, key, Status.UNKNOWN, null, Collections.emptyList());
  }

  public static ListObject fromAbsent(CheckContext ctx, String key) {
    return new ListObject(ctx, null, key, Status.ABSENT, null, Collections.emptyList());
  }

  public Stream<YamlTree> getItemIf(Predicate<YamlTree> predicate) {
    return items.stream().filter(predicate);
  }

  public ListObject reportItemIf(Predicate<YamlTree> predicate, String message) {
    getItemIf(predicate).forEach(item -> ctx.reportIssue(item, message));
    return this;
  }

  public ListObject reportIfAnyItem(Predicate<YamlTree> predicate, String message) {
    getItemIf(predicate).findFirst().ifPresent(item -> reportOnItems(message));
    return this;
  }

  public ListObject reportOnItems(String message) {
    if (!items.isEmpty()) {
      var merged = TextRanges.merge(items.stream()
        .map(HasTextRange::textRange)
        .toList());
      ctx.reportIssue(merged, message);
    }
    return this;
  }

  public void forEachElementAsBlock(Consumer<BlockObject> consumer) {
    items.stream()
      .map(item -> BlockObject.fromPresent(ctx, item, null))
      .forEach(consumer);
  }
}
