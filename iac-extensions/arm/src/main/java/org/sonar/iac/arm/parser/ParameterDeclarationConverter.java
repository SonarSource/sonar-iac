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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.SimpleProperty;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.json.ParameterDeclarationImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.MappingTreeImpl;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

public class ParameterDeclarationConverter extends ArmBaseConverter {

  public ParameterDeclarationConverter(@Nullable InputFileContext inputFileContext) {
    super(inputFileContext);
  }

  public Stream<TupleTree> extractParametersSequence(FileTree fileTree) {
    MappingTree document = (MappingTree) fileTree.documents().get(0);
    return document.elements().stream()
      .filter(element -> element.key() instanceof ScalarTree)
      .filter(element -> "parameters".equals(((ScalarTree) element.key()).value()))
      .map(TupleTree::value)
      .map(MappingTree.class::cast)
      .map(MappingTree::elements)
      .flatMap(List::stream);
  }

  public ParameterDeclaration convertParameters(TupleTree tupleTree) {
    String id = ((ScalarTree) tupleTree.key()).value();
    Identifier identifier = new IdentifierImpl(id, tupleTree.key().metadata());

    Map<String, Property> properties = extractProperties(((MappingTreeImpl) tupleTree.value()));

    SimpleProperty type = extractMandatorySimpleProperty(tupleTree.metadata(), properties, "type");
    Property defaultValue = extractProperty(properties, "defaultValue");
    SimpleProperty minValue = extractSimpleProperty(properties, "minValue");
    SimpleProperty maxValue = extractSimpleProperty(properties, "maxValue");
    SimpleProperty minLength = extractSimpleProperty(properties, "minLength");
    SimpleProperty maxLength = extractSimpleProperty(properties, "maxLength");
    ArrayExpression allowedValues = extractArrayExpression(properties, "allowedValues");
    SimpleProperty description = null;

    if (properties.containsKey("metadata")) {
      ObjectExpression copy = toObjectExpression(properties.remove("metadata").value());
      description = convertToSimpleProperty(copy.getPropertyByName("description"));
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

  private List<Expression> prepareListAllowedValues(@Nullable ArrayExpression allowedValues) {
    if (allowedValues == null) {
      return Collections.emptyList();
    }
    return allowedValues.values().stream()
      .map(this::toExpression)
      .collect(Collectors.toList());
  }
}
