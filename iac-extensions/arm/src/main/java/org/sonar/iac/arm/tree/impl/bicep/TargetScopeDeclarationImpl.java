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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.bicep.StringComplete;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class TargetScopeDeclarationImpl extends AbstractArmTreeImpl implements TargetScopeDeclaration {

  private final SyntaxToken keyword;
  private final SyntaxToken equals;
  private final Expression expression;

  public TargetScopeDeclarationImpl(SyntaxToken keyword, SyntaxToken equals, Expression expression) {
    this.keyword = keyword;
    this.equals = equals;
    this.expression = expression;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword, equals, expression);
  }

  @Override
  public Expression value() {
    return expression;
  }

  @Override
  public Kind getKind() {
    return Kind.TARGET_SCOPE_DECLARATION;
  }

  @Override
  public File.Scope scope() {
    if (expression.is(Kind.STRING_COMPLETE)) {
      StringComplete stringLiteral = (StringComplete) expression;
      switch (stringLiteral.value()) {
        case "managementGroup":
          return File.Scope.MANAGEMENT_GROUP;
        case "resourceGroup":
          return File.Scope.RESOURCE_GROUP;
        case "subscription":
          return File.Scope.SUBSCRIPTION;
        case "tenant":
          return File.Scope.TENANT;
        default:
          return File.Scope.UNKNOWN;
      }
    } else {
      return File.Scope.UNKNOWN;
    }
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }
}
