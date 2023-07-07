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
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NullLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.impl.bicep.BooleanLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.FileImpl;
import org.sonar.iac.arm.tree.impl.bicep.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.bicep.MetadataDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.InterpolatedStringImpl;
import org.sonar.iac.arm.tree.impl.bicep.NullLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.NumericLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.StringLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.arm.tree.impl.bicep.TargetScopeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.VariableDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringRightPiece;

public class TreeFactory {

  // Ignore unused method parameters
  @SuppressWarnings("java:S1172")
  public File file(Optional<List<Statement>> statements, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileImpl(statements.or(Collections.emptyList()), eof);
  }

  public TargetScopeDeclaration targetScopeDeclaration(SyntaxToken keyword, SyntaxToken equals, Expression expression) {
    return new TargetScopeDeclarationImpl(keyword, equals, expression);
  }

  public MetadataDeclaration metadataDeclaration(SyntaxToken keyword, Identifier identifier, SyntaxToken equals,
    Expression expression, SyntaxToken newLine) {
    return new MetadataDeclarationImpl(keyword, identifier, equals, expression, newLine);
  }

  public VariableDeclaration variableDeclaration(SyntaxToken keyword, Identifier identifier, SyntaxToken equals, Expression expression, SyntaxToken newLine) {
    return new VariableDeclarationImpl(keyword, identifier, equals, expression, newLine);
  }

  public InterpolatedString interpolatedString(InterpolatedStringLeftPiece stringLeftPiece,
    Optional<List<InterpolatedStringMiddlePiece>> stringMiddlePieces,
    InterpolatedStringRightPiece stringRightPiece) {
    return new InterpolatedStringImpl(stringLeftPiece, stringMiddlePieces.or(List.of()), stringRightPiece);
  }

  public InterpolatedStringLeftPiece interpolatedStringLeftPiece(SyntaxToken leftQuote, Optional<SyntaxToken> stringChars, SyntaxToken dollarLcurly) {
    return new InterpolatedStringLeftPiece(leftQuote, stringChars, dollarLcurly);
  }

  public InterpolatedStringMiddlePiece interpolatedStringMiddlePiece(Expression expression, SyntaxToken rCurly, Optional<SyntaxToken> stringChars, SyntaxToken dollarLcurly) {
    return new InterpolatedStringMiddlePiece(expression, rCurly, stringChars, dollarLcurly);
  }

  public InterpolatedStringRightPiece interpolatedStringRightPiece(Expression expression, SyntaxToken rCurly, Optional<SyntaxToken> stringChars, SyntaxToken rightQuote) {
    return new InterpolatedStringRightPiece(expression, rCurly, stringChars, rightQuote);
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

  public NumericLiteral numericLiteral(SyntaxToken token) {
    return new NumericLiteralImpl(token);
  }

  public BooleanLiteral booleanLiteral(SyntaxToken token) {
    return new BooleanLiteralImpl(token);
  }

  public NullLiteral nullLiteral(SyntaxToken token) {
    return new NullLiteralImpl(token);
  }
}
