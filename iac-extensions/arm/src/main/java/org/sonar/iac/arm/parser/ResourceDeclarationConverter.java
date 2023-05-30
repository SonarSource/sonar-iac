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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.SimpleProperty;
import org.sonar.iac.arm.tree.impl.json.ResourceDeclarationImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

public class ResourceDeclarationConverter extends ArmBaseConverter {

  public ResourceDeclarationConverter(@Nullable InputFileContext inputFileContext) {
    super(inputFileContext);
  }

  public Optional<SequenceTree> extractResourcesSequence(MappingTree document) {
    return document.elements().stream()
      .filter(element -> element.key() instanceof ScalarTree)
      .filter(element -> "resources".equals(((ScalarTree) element.key()).value()))
      .map(TupleTree::value)
      .filter(SequenceTree.class::isInstance)
      .map(SequenceTree.class::cast)
      .findFirst();
  }

  public List<ResourceDeclaration> convertResources(SequenceTree resource) {
    return resource.elements().stream()
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .map(this::convertToResourceDeclaration)
      .collect(Collectors.toList());
  }

  private ResourceDeclaration convertToResourceDeclaration(MappingTree tree) {
    Map<String, Property> properties = extractProperties(tree);

    SimpleProperty type = extractMandatorySimpleProperty(tree.metadata(), properties, "type");
    SimpleProperty version = extractMandatorySimpleProperty(tree.metadata(), properties, "apiVersion");
    SimpleProperty name = extractMandatorySimpleProperty(tree.metadata(), properties, "name");
    List<Property> otherProperties = new ArrayList<>(properties.values());

    return new ResourceDeclarationImpl(name, version, type, otherProperties);
  }
}
