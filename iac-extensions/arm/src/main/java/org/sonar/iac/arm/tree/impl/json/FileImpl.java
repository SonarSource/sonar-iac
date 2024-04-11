/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.arm.symbols.SymbolTable;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.yaml.tree.MappingTree;

public class FileImpl extends AbstractArmTreeImpl implements File {

  @Nullable
  private final StringLiteral targetScope;
  private final List<Statement> statements;
  private final MappingTree document;
  private SymbolTable symbolTable;

  public FileImpl(@Nullable StringLiteral targetScope, List<Statement> statements, MappingTree document) {
    this.targetScope = targetScope;
    this.statements = statements;
    this.document = document;
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

    return switch (scopeSuffix) {
      case "/managementGroupDeploymentTemplate.json#" -> Scope.MANAGEMENT_GROUP;
      case "/deploymentTemplate.json#" -> Scope.RESOURCE_GROUP;
      case "/subscriptionDeploymentTemplate.json#" -> Scope.SUBSCRIPTION;
      case "/tenantDeploymentTemplate.json#" -> Scope.TENANT;
      default -> Scope.UNKNOWN;
    };
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

  public MappingTree document() {
    return document;
  }

  @Override
  public Kind getKind() {
    return Kind.FILE;
  }

  @Nullable
  @Override
  public SymbolTable symbolTable() {
    return symbolTable;
  }

  @Override
  public void setSymbolTable(SymbolTable symbolTable) {
    if (this.symbolTable != null) {
      throw new IllegalArgumentException("A symbolTable is already set");
    }
    this.symbolTable = symbolTable;
  }
}
