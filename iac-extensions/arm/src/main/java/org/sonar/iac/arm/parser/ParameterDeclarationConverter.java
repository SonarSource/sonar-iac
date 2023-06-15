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

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.json.ParameterDeclarationImpl;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

public class ParameterDeclarationConverter extends ArmBaseConverter {

  public ParameterDeclarationConverter(@Nullable InputFileContext inputFileContext) {
    super(inputFileContext);
  }

  public Stream<TupleTree> extractParametersSequence(MappingTree document) {
    return document.elements().stream()
      .filter(filterOnField("parameters"))
      .map(TupleTree::value)
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .map(MappingTree::elements)
      .flatMap(List::stream);
  }

  public ParameterDeclaration convertParameters(TupleTree tree) {
    Identifier identifier = toIdentifier(tree.key());
    StringLiteral type = PropertyUtils.get(tree.value(), "type").map(this::toStringLiteral).orElse(null);
    Expression defaultValue = PropertyUtils.get(tree.value(), "defaultValue").map(this::toExpression).orElse(null);
    NumericLiteral minValue = PropertyUtils.get(tree.value(), "minValue").map(this::toNumericLiteral).orElse(null);
    NumericLiteral maxValue = PropertyUtils.get(tree.value(), "maxValue").map(this::toNumericLiteral).orElse(null);
    NumericLiteral minLength = PropertyUtils.get(tree.value(), "minLength").map(this::toNumericLiteral).orElse(null);
    NumericLiteral maxLength = PropertyUtils.get(tree.value(), "maxLength").map(this::toNumericLiteral).orElse(null);
    ArrayExpression allowedValues = PropertyUtils.get(tree.value(), "allowedValues").map(this::toArrayExpression).orElse(null);
    StringLiteral description = PropertyUtils.get(tree.value(), "metadata").map(this::extractDescriptionFromMetadata).orElse(null);

    return new ParameterDeclarationImpl(identifier, type, defaultValue, allowedValues, description, minValue, maxValue, minLength, maxLength);
  }

  @CheckForNull
  private StringLiteral extractDescriptionFromMetadata(PropertyTree metadataProperty) {
    return PropertyUtils.get(metadataProperty.value(), "description")
      .map(this::toStringLiteral)
      .orElse(null);
  }
}
