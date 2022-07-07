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
import org.sonar.iac.common.yaml.tree.YamlTree;

public class SequenceBlockSymbol extends BlockSymbol<SequenceBlockSymbol> {

  SequenceBlockSymbol(CheckContext ctx, @Nullable MappingTree tree, String key, @Nullable BlockSymbol<?> parent) {
    super(ctx, tree, key, parent);
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree;
  }

  public static SequenceBlockSymbol fromPresent(CheckContext ctx, YamlTree tree, String key, BlockSymbol<?> parent) {
    if (tree instanceof MappingTree) {
      return new SequenceBlockSymbol(ctx, (MappingTree) tree, key, parent);
    }
    return fromAbsent(ctx, key, parent);
  }

  public static SequenceBlockSymbol fromAbsent(CheckContext ctx, String key, BlockSymbol<?> parent) {
    return new SequenceBlockSymbol(ctx, null, key, parent);
  }
}
