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
package org.sonar.iac.common.yaml.object;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class ListObject extends YamlObject<ListObject, SequenceTree>{

  private final List<YamlTree> items;
  private final YamlTree parent;

  ListObject(CheckContext ctx, @Nullable SequenceTree tree, String key, Status status, @Nullable YamlTree parent, List<YamlTree> items) {
    super(ctx, tree, key, status);
    this.parent = parent;
    this.items = items;
  }

  public static ListObject fromPresent(CheckContext ctx, YamlTree tree, String key, YamlTree parent) {
    if(tree instanceof TupleTree) {
      return fromPresent(ctx, ((TupleTree) tree).value(), key, tree);
    }
    if (tree instanceof SequenceTree) {
      return new ListObject(ctx, ((SequenceTree) tree), key, Status.PRESENT, parent, ((SequenceTree)tree).elements());
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

  public ListObject reportIfAnyItem(Predicate<YamlTree> predicate, String message) {
    getItemIf(predicate).findFirst().ifPresent(item -> report(message));
    return this;
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return parent;
  }
}
