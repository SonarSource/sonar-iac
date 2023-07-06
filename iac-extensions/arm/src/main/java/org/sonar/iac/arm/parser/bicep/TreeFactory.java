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
package org.sonar.iac.arm.parser.bicep;

import com.sonar.sslr.api.typed.Optional;
import java.util.Collections;
import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.impl.bicep.FileImpl;
import org.sonar.iac.arm.tree.impl.bicep.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.bicep.StringLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.TargetScopeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.VariableDeclarationImpl;

public class TreeFactory {

  // Ignore unused method parameters
  @SuppressWarnings("java:S1172")
  public File file(Optional<List<Statement>> statements, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileImpl(statements.or(Collections.emptyList()), eof);
  }

  public TargetScopeDeclaration targetScopeDeclaration(SyntaxToken targetScope, SyntaxToken equals, Expression expression) {
    return new TargetScopeDeclarationImpl(targetScope, equals, expression);
  }

  public VariableDeclaration variableDeclaration(SyntaxToken variableKeyword, Identifier identifier, SyntaxToken equals, Expression expression) {
    return new VariableDeclarationImpl(variableKeyword, identifier, equals, expression);
  }

  public StringLiteral stringLiteral(SyntaxToken token) {
    return new StringLiteralImpl(token);
  }

  public Identifier identifier(SyntaxToken token) {
    return new IdentifierImpl(token);
  }

  // Ignore unused method parameters
  @SuppressWarnings("java:S1172")
  public <T, U> U ignoreFirst(T first, U second) {
    return second;
  }

  // Ignore unused method parameters
  @SuppressWarnings("java:S1172")
  public <T, U> T ignoreLast(T first, U second) {
    return first;
  }
}
