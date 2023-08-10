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
package org.sonar.iac.arm.parser;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.json.ParameterDeclarationImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

public class ParameterDeclarationConverter extends ArmJsonBaseConverter {

  public ParameterDeclarationConverter(@Nullable InputFileContext inputFileContext) {
    super(inputFileContext);
  }

  public Stream<TupleTree> extractParametersSequence(MappingTree document) {
    return extractMappingToTupleTreeOnField(document, "parameters");
  }

  public ParameterDeclaration convertParameters(TupleTree tree) {
    Identifier identifier = toIdentifier(tree.key());
    StringLiteral type = toStringLiteralOrNull(tree.value(), "type");
    Expression defaultValue = toExpressionOrNull(tree, "defaultValue");
    NumericLiteral minValue = toNumericLiteralOrNull(tree.value(), "minValue");
    NumericLiteral maxValue = toNumericLiteralOrNull(tree.value(), "maxValue");
    NumericLiteral minLength = toNumericLiteralOrNull(tree.value(), "minLength");
    NumericLiteral maxLength = toNumericLiteralOrNull(tree.value(), "maxLength");
    ArrayExpression allowedValues = toArrayExpressionOrNull(tree.value(), "allowedValues");
    StringLiteral description = toNestedStringLiteralOrNull(tree.value(), "metadata", "description");

    return new ParameterDeclarationImpl(identifier, type, defaultValue, allowedValues, description, minValue, maxValue, minLength, maxLength);
  }
}
