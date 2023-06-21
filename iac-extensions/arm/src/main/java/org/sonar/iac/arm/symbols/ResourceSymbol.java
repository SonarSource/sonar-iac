/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.arm.symbols;

import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.dsl.MapSymbol;

public class ResourceSymbol extends MapSymbol<ResourceSymbol, ResourceDeclaration> {

  public final String type;
  public final String version;

  public static ResourceSymbol fromPresent(CheckContext ctx, ResourceDeclaration tree) {
    return new ResourceSymbol(ctx, tree.type().value(), tree);
  }

  public static ResourceSymbol fromParent(ResourceSymbol parent, ResourceDeclaration child) {
    String type = parent.type + "/" + child.type().value();
    return new ResourceSymbol(parent.ctx, type, child);
  }

  protected ResourceSymbol(CheckContext ctx, String type, ResourceDeclaration tree) {
    super(ctx, tree, tree.name().value(), null);
    this.type = type;
    this.version = tree.version().value();
  }

  @Override
  public ResourceSymbol reportIfAbsent(String message, SecondaryLocation... secondaries) {
    throw new UnsupportedOperationException("Resource symbols should always exists");
  }

  @Nullable
  @Override
  protected HasTextRange toHighlight() {
    return tree != null ? tree.type() : null;
  }
}
