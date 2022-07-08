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

import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class TupleObject extends AttributeObject<TupleObject, TupleTree> {

  TupleObject(CheckContext ctx, @Nullable TupleTree tree, String key, Status status) {
    super(ctx, tree, key, status);
  }

  public static TupleObject fromPresent(CheckContext ctx, YamlTree tree, String key) {
    if (tree instanceof TupleTree) {
      return new TupleObject(ctx, (TupleTree) tree, key, Status.PRESENT);
    }
    return new TupleObject(ctx, null, key, Status.UNKNOWN);
  }

  public static TupleObject fromAbsent(CheckContext ctx, String key) {
    return new TupleObject(ctx, null, key, Status.ABSENT);
  }

  public TupleObject reportIfValue(Predicate<YamlTree> predicate, String message) {
    if (tree != null && predicate.test(tree.value())) {
      report(message);
    }
    return this;
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree;
  }



}
