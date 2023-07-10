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
import java.util.List;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NullLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.FunctionCall;
import org.sonar.iac.arm.tree.api.bicep.FunctionDeclaration;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TypeDeclaration;
import org.sonar.iac.arm.tree.impl.bicep.BooleanLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.FileImpl;
import org.sonar.iac.arm.tree.impl.bicep.FunctionCallImpl;
import org.sonar.iac.arm.tree.impl.bicep.FunctionDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.bicep.InterpolatedStringImpl;
import org.sonar.iac.arm.tree.impl.bicep.MetadataDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.NullLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.NumericLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.ObjectExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.PropertyImpl;
import org.sonar.iac.arm.tree.impl.bicep.ResourceDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.StringLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.TargetScopeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.VariableDeclarationImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.impl.Tuple;

import static java.util.Collections.emptyList;
import static org.sonar.iac.common.api.tree.impl.SeparatedListImpl.emptySeparatedList;
import static org.sonar.iac.common.api.tree.impl.SeparatedListImpl.separatedList;

public class TreeFactory {

  // Ignore unused method parameters
  @SuppressWarnings("java:S1172")
  public File file(Optional<List<Statement>> statements, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileImpl(statements.or(emptyList()), eof);
  }

  // TODO SONARIAC-951 Put in place decorator
  public TypeDeclaration typeDeclaration(SyntaxToken keyword, Identifier name, SyntaxToken equ, StringLiteral typeExpression) {
    return new TypeDeclarationImpl(keyword, name, equ, typeExpression);
  }

  public TargetScopeDeclaration targetScopeDeclaration(SyntaxToken keyword, SyntaxToken equals, Expression expression) {
    return new TargetScopeDeclarationImpl(keyword, equals, expression);
  }

  public FunctionDeclaration functionDeclaration(SyntaxToken func, Identifier name, StringLiteral lambdaExpression) {
    return new FunctionDeclarationImpl(func, name, lambdaExpression);
  }

  public MetadataDeclaration metadataDeclaration(SyntaxToken keyword, Identifier identifier, SyntaxToken equals,
    Expression expression, SyntaxToken newLine) {
    return new MetadataDeclarationImpl(keyword, identifier, equals, expression, newLine);
  }

  public VariableDeclaration variableDeclaration(SyntaxToken keyword, Identifier identifier, SyntaxToken equals, Expression expression, SyntaxToken newLine) {
    return new VariableDeclarationImpl(keyword, identifier, equals, expression, newLine);
  }

  public StringLiteral stringLiteral(SyntaxToken token) {
    return new StringLiteralImpl(token);
  }

  // Ignore unused method parameters
  @SuppressWarnings("java:S1172")
  public ResourceDeclaration resourceDeclaration(
    SyntaxToken keyword,
    Identifier identifier,
    InterpolatedString type,
    Optional<SyntaxToken> existing,
    SyntaxToken equalsSign,
    ObjectExpression objectExpression,
    SyntaxToken endOfLine) {
    return new ResourceDeclarationImpl(keyword, identifier, type, existing.orNull(), equalsSign, objectExpression, endOfLine);
  }

  public FunctionCall functionCall(Identifier identifier, SyntaxToken leftParenthesis, Optional<SeparatedList<Expression, SyntaxToken>> argumentList,
    SyntaxToken rightParenthesis) {
    return new FunctionCallImpl(identifier, leftParenthesis, argumentList.or(emptySeparatedList()), rightParenthesis);
  }

  public SeparatedList<Expression, SyntaxToken> functionCallArguments(Expression firstArgument,
    Optional<List<Tuple<SyntaxToken, Expression>>> additionalArguments) {
    return separatedList(firstArgument, additionalArguments);
  }

  public Identifier identifier(SyntaxToken token) {
    return new IdentifierImpl(token);
  }

  public InterpolatedString interpolatedString(SyntaxToken openingApostrophe, SyntaxToken value, SyntaxToken closingApostrophe) {
    return new InterpolatedStringImpl(openingApostrophe, value, closingApostrophe);
  }

  public Property objectProperty(Identifier key, SyntaxToken colon, Expression value) {
    return new PropertyImpl(key, colon, value);
  }

  public ObjectExpression objectExpression(SyntaxToken leftCurlyBrace, Optional<List<Property>> properties, SyntaxToken rightCurlyBrace) {
    return new ObjectExpressionImpl(leftCurlyBrace, properties.or(emptyList()), rightCurlyBrace);
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

  public <T, U> Tuple<T, U> newTuple(T first, U second) {
    return new Tuple<>(first, second);
  }
}
