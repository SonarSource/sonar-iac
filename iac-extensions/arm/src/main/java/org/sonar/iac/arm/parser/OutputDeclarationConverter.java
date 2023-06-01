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
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.PropertyValue;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.json.OutputDeclarationImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

public class OutputDeclarationConverter extends ArmBaseConverter {

  private static final Logger LOG = Loggers.get(OutputDeclarationConverter.class);

  public OutputDeclarationConverter(@Nullable InputFileContext inputFileContext) {
    super(inputFileContext);
  }

  public Stream<TupleTree> extractOutputsMapping(MappingTree document) {
    return document.elements().stream()
      .filter(filterOnField("outputs"))
      .map(TupleTree::value)
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .map(MappingTree::elements)
      .flatMap(List::stream);
  }

  public OutputDeclaration convertOutputDeclaration(TupleTree tupleTree) {
    Identifier name = convertToIdentifier(tupleTree.key());

    MappingTree outputMapping = toMappingTree(tupleTree.value());
    Map<String, Property<PropertyValue>> properties = extractProperties(outputMapping);

    Property<StringLiteral> type = extractMandatoryProperty(tupleTree.metadata(), properties, "type", ArmTree.Kind.STRING_LITERAL);
    Property<StringLiteral> condition = extractProperty(properties, "condition", ArmTree.Kind.STRING_LITERAL);
    Property<StringLiteral> value = extractProperty(properties, "value", ArmTree.Kind.STRING_LITERAL);
    Property<StringLiteral> copyCount = null;
    Property<StringLiteral> copyInput = null;

    if (properties.containsKey("copy")) {
      ObjectExpression copy = toObjectExpression(properties.remove("copy").value());
      copyCount = toProperty(copy.getPropertyByName("count"), ArmTree.Kind.STRING_LITERAL);
      copyInput = toProperty(copy.getPropertyByName("input"), ArmTree.Kind.STRING_LITERAL);
    }

    for (Map.Entry<String, Property<PropertyValue>> unexpectedProperty : properties.entrySet()) {
      TextRange position = unexpectedProperty.getValue().textRange();
      LOG.debug("Unexpected property '{}' found in output declaration at {}, ignoring it.", unexpectedProperty.getKey(), filenameAndPosition(position));
    }

    return new OutputDeclarationImpl(name, type, condition, copyCount, copyInput, value);
  }
}
