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
package org.sonar.iac.arm.checkdsl;

import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.TextTree;

public final class ContextualResource extends ContextualMap<ContextualResource, ResourceDeclaration> {

  public final String type;
  public final String version;

  private ContextualResource(CheckContext ctx, ResourceDeclaration tree, String type) {
    super(ctx, tree, tree.name().value(), null);
    this.type = type;
    this.version = Optional.ofNullable(tree.version()).map(TextTree::value).orElse("");
  }

  public static ContextualResource fromPresent(CheckContext ctx, ResourceDeclaration tree) {
    return new ContextualResource(ctx, tree, tree.type().value());
  }

  public static ContextualResource fromPresent(CheckContext ctx, ResourceDeclaration tree, String resourceType) {
    return new ContextualResource(ctx, tree, resourceType);
  }

  @Override
  public ContextualResource reportIfAbsent(String message, SecondaryLocation... secondaries) {
    throw new UnsupportedOperationException("Resource tree should always exists");
  }

  @CheckForNull
  @Override
  protected HasTextRange toHighlight() {
    return tree != null ? tree.type() : null;
  }
}
