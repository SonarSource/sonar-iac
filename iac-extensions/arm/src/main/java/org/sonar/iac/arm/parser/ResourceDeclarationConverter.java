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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.json.ResourceGroupDeclarationImpl;
import org.sonar.iac.arm.tree.impl.json.ResourceDeclarationImpl;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class ResourceDeclarationConverter extends ArmBaseConverter {

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
      .collect(Collectors.toList());
  }

  public ResourceDeclaration convertToResourceDeclaration(MappingTree tree) {
    return convertToResourceDeclaration(tree, null);
  }

  public ResourceDeclaration convertToResourceDeclaration(MappingTree tree, @Nullable ResourceDeclaration parentResource) {
    StringLiteral type = toStringLiteralOrException(tree, "type");
    StringLiteral version = toStringLiteralOrException(tree, "apiVersion");
    Identifier name = toIdentifierOrException(tree, "name");
    List<Property> otherProperties = PropertyUtils.get(tree, "properties")
      .map(PropertyTree::value)
      .map(this::toProperties)
      .orElse(Collections.emptyList());

    Optional<PropertyTree> optChildResources = PropertyUtils.get(tree, "resources");
    if (optChildResources.isPresent()) {
      List<ResourceDeclaration> childResources = optChildResources
        .map(PropertyTree::value)
        .filter(SequenceTree.class::isInstance)
        .map(SequenceTree.class::cast)
        .map(sequenceTree -> mappingTreeOnly(sequenceTree.elements()))
        .map(m -> m.stream().map(mappingTree -> convertToResourceDeclaration(mappingTree, parentResource)).collect(Collectors.toList()))
        .orElse(Collections.emptyList());
      return new ResourceGroupDeclarationImpl(parentResource, name, version, type, otherProperties, childResources);
    } else {
      return new ResourceDeclarationImpl(parentResource, name, version, type, otherProperties);
    }
  }
}
