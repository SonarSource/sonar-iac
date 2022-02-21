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
package org.sonar.iac.terraform.symbols;

import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.terraform.api.tree.BlockTree;

public class ResourceSymbol extends BlockSymbol {

  public final String type;

  private ResourceSymbol(CheckContext ctx, BlockTree tree) {
    super(ctx, tree, tree.labels().size() < 2 ? "unknown" : tree.labels().get(1).value(), null);
    type = tree.labels().isEmpty() ? "" : tree.labels().get(0).value();
  }

  public static ResourceSymbol fromPresent(CheckContext ctx, BlockTree tree) {
    return new ResourceSymbol(ctx, tree);
  }

  @Override
  public ResourceSymbol reportIfAbsent(String message, SecondaryLocation... secondaries) {
    throw new UnsupportedOperationException("Resource symbols should always exists");
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree.labels().isEmpty() ? null : tree.labels().get(0);
  }
}
