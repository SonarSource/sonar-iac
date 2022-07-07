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
package org.sonar.iac.kubernetes.symbols;

import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class TupleBlockSymbol extends BlockSymbol<TupleBlockSymbol> {


  private final HasTextRange toHighlight;

  TupleBlockSymbol(CheckContext ctx, @Nullable HasTextRange toHighlight, @Nullable MappingTree block, String key, BlockSymbol<?> parent) {
    super(ctx, block, key, parent);
    this.toHighlight = toHighlight;
  }

  public static TupleBlockSymbol fromPresent(CheckContext ctx, YamlTree tree, String key, BlockSymbol<?> parent) {
    if (tree instanceof TupleTree && ((TupleTree) tree).value() instanceof MappingTree) {
      TupleTree tuple = (TupleTree) tree;
      return new TupleBlockSymbol(ctx, tuple.key(), (MappingTree) tuple.value(), key, parent);
    }
    return fromAbsent(ctx, key, parent);
  }

  public static TupleBlockSymbol fromAbsent(CheckContext ctx, String key, BlockSymbol<?> parent) {
    return new TupleBlockSymbol(ctx, null, null, key, parent);
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return toHighlight;
  }
}
