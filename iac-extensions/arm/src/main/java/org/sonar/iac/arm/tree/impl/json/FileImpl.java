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
package org.sonar.iac.arm.tree.impl.json;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class FileImpl extends AbstractArmTreeImpl implements File {

  @Nullable
  private final StringLiteral targetScope;
  private final List<Statement> statements;

  public FileImpl(@Nullable StringLiteral targetScope, List<Statement> statements) {
    this.targetScope = targetScope;
    this.statements = statements;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(statements);
  }

  @Override
  public Scope targetScope() {
    if (targetScope == null) {
      return Scope.NOT_SET;
    }

    String scopeAsString = targetScope.value();
    String scopeSuffix = scopeAsString.substring(scopeAsString.lastIndexOf('/'));

    switch (scopeSuffix) {
      case "/managementGroupDeploymentTemplate.json#":
        return Scope.MANAGEMENT_GROUP;
      case "/deploymentTemplate.json#":
        return Scope.RESOURCE_GROUP;
      case "/subscriptionDeploymentTemplate.json#":
        return Scope.SUBSCRIPTION;
      case "/tenantDeploymentTemplate.json#":
        return Scope.TENANT;
      default:
        return Scope.UNKNOWN;
    }
  }

  @CheckForNull
  @Override
  public StringLiteral targetScopeLiteral() {
    return targetScope;
  }

  @Override
  public List<Statement> statements() {
    return statements;
  }

  @Override
  public Kind getKind() {
    return Kind.FILE;
  }
}
