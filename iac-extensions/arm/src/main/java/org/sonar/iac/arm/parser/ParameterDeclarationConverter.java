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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.json.ParameterDeclarationImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.MappingTreeImpl;
import org.sonar.iac.common.yaml.tree.ScalarTree;
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

  public ParameterDeclaration convertParameters(TupleTree tupleTree) {
    String id = ((ScalarTree) tupleTree.key()).value();
    Identifier identifier = new IdentifierImpl(id, tupleTree.key().metadata());

    Map<String, Property> properties = extractProperties(((MappingTreeImpl) tupleTree.value()));

    Property type = extractMandatoryProperty(tupleTree.metadata(), properties, "type", ArmTree.Kind.STRING_LITERAL);
    Property defaultValue = extractProperty(properties, "defaultValue");
    Property minValue = extractProperty(properties, "minValue", ArmTree.Kind.NUMERIC_LITERAL);
    Property maxValue = extractProperty(properties, "maxValue", ArmTree.Kind.NUMERIC_LITERAL);
    Property minLength = extractProperty(properties, "minLength", ArmTree.Kind.NUMERIC_LITERAL);
    Property maxLength = extractProperty(properties, "maxLength", ArmTree.Kind.NUMERIC_LITERAL);
    ArrayExpression allowedValues = extractArrayExpression(properties, "allowedValues");
    Property description = null;

    if (properties.containsKey("metadata")) {
      ObjectExpression copy = toObjectExpression(properties.remove("metadata").value());
      description = checkPropertyType(copy.getPropertyByName("description"), ArmTree.Kind.STRING_LITERAL);
    }

    checkUnexpectedProperties(properties, id);

    return new ParameterDeclarationImpl(
      identifier,
      type,
      defaultValue,
      prepareListAllowedValues(allowedValues),
      description,
      minValue,
      maxValue,
      minLength,
      maxLength);
  }

  private static List<Expression> prepareListAllowedValues(@Nullable ArrayExpression allowedValues) {
    if (allowedValues == null) {
      return Collections.emptyList();
    }
    return allowedValues.elements();
  }
}
