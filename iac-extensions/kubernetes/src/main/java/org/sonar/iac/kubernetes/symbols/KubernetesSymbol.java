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

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.tree.YamlTree;

abstract class KubernetesSymbol<T extends KubernetesSymbol<?, ?>, K extends YamlTree> {

  public final CheckContext ctx;
  public final @Nullable K tree;
  private final String key;
  private final BlockSymbol<?, ?> parent;


  protected KubernetesSymbol(CheckContext ctx, @Nullable K tree, String key, BlockSymbol<?, ?> parent) {
    this.ctx = ctx;
    this.tree = tree;
    this.key = key;
    this.parent = parent;
  }

  @Nullable
  protected abstract HasTextRange toHighlight();

  public T reportIfTree(Predicate<YamlTree> predicate, String message) {
    if (predicate.test(tree)) {
      if (tree == null) {
        parent.report(message);
      } else {
        report(message);
      }
    }
    return (T) this;
  }

  public void report(String message) {
    Optional.ofNullable(toHighlight())
      .ifPresent(hasTextRange -> ctx.reportIssue(hasTextRange, message));
  }
}
