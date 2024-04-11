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
package org.sonar.iac.arm.parser;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.json.PropertyImpl;
import org.sonar.iac.arm.tree.impl.json.ResourceDeclarationImpl;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class ResourceDeclarationConverter extends ArmJsonBaseConverter {

  public ResourceDeclarationConverter(@Nullable InputFileContext inputFileContext) {
    super(inputFileContext);
  }

  public Stream<MappingTree> extractResourcesSequence(MappingTree document) {
    return document.elements().stream()
      .filter(filterOnField("resources"))
      .map(TupleTree::value)
      .filter(SequenceTree.class::isInstance)
      .map(SequenceTree.class::cast)
      .map(sequenceTree -> mappingTreeOnly(sequenceTree.elements()))
      .flatMap(List::stream);
  }

  private static List<MappingTree> mappingTreeOnly(List<YamlTree> yamlTrees) {
    return yamlTrees.stream()
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .toList();
  }

  public ResourceDeclaration convertToResourceDeclaration(MappingTree tree) {
    StringLiteral type = toStringLiteralOrException(tree, "type");
    var version = toExpressionOrException(tree, "apiVersion");
    StringLiteral name = toStringLiteralOrException(tree, "name");
    List<Property> resourceProperties = toResourceProperties(tree);
    List<Property> otherProperties = PropertyUtils.get(tree, "properties"::equalsIgnoreCase)
      .map(PropertyTree::value)
      .map(this::toProperties)
      .orElse(Collections.emptyList());

    return PropertyUtils.get(tree, "resources")
      .map(childResources -> toResourceDeclarationWithChildren(type, version, name, otherProperties, resourceProperties, childResources))
      .orElseGet(() -> toResourceDeclaration(type, version, name, otherProperties, resourceProperties));
  }

  private List<Property> toResourceProperties(MappingTree tree) {
    return tree.elements().stream()
      .map(tupleTree -> {
        Identifier key = toIdentifier(tupleTree.key());
        Expression value = toExpression(tupleTree.value());
        return new PropertyImpl(key, value);
      })
      .collect(Collectors.toList());
  }

  private static ResourceDeclaration toResourceDeclaration(StringLiteral type,
    Expression version,
    StringLiteral name,
    List<Property> otherProperties,
    List<Property> resourceProperties) {
    return new ResourceDeclarationImpl(name, version, type, otherProperties, resourceProperties, Collections.emptyList());
  }

  private ResourceDeclaration toResourceDeclarationWithChildren(StringLiteral type,
    Expression version,
    StringLiteral name,
    List<Property> otherProperties,
    List<Property> resourceProperties,
    PropertyTree childResourcesProperty) {
    List<ResourceDeclaration> childResources = Optional.of(childResourcesProperty.value())
      .filter(SequenceTree.class::isInstance)
      .map(SequenceTree.class::cast)
      .map(sequenceTree -> mappingTreeOnly(sequenceTree.elements()))
      .map(m -> m.stream().map(this::convertToResourceDeclaration).toList())
      .orElse(Collections.emptyList());
    return new ResourceDeclarationImpl(name, version, type, otherProperties, resourceProperties, childResources);
  }
}
