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
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.impl.ExpressionImpl;
import org.sonar.iac.arm.tree.impl.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.PropertyImpl;
import org.sonar.iac.arm.tree.impl.ResourceDeclarationImpl;
import org.sonar.iac.arm.tree.impl.json.FileImpl;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class ArmParser implements TreeParser<ArmTree> {

  @Override
  public ArmTree parse(String source, @Nullable InputFileContext inputFileContext) {
    return convert(parseJson(source, inputFileContext));
  }

  private static FileTree parseJson(String source, @Nullable InputFileContext inputFileContext) {
    YamlParser yamlParser = new YamlParser();
    try {
      return yamlParser.parse(source, inputFileContext);
    } catch (Exception e) {
      String message = "Failed to parse";
      if (inputFileContext != null) {
        String filename = inputFileContext.inputFile.filename();
        message = message + " " + filename;
      }
      throw new ParseException(message, null, e.getMessage());
    }
  }

  private static ArmTree convert(FileTree fileTree) {
    List<Statement> statements = new ArrayList<>();

    extractResourcesSequence(fileTree).ifPresent(res -> {
      List<ResourceDeclaration> resourceDeclarations = convertResources(res);
      statements.addAll(resourceDeclarations);
    });

    return new FileImpl(statements);
  }

  private static Optional<SequenceTree> extractResourcesSequence(FileTree fileTree) {
    MappingTree document = (MappingTree) fileTree.documents().get(0);
    return document.elements().stream()
      .filter(element -> element.key() instanceof ScalarTree)
      .filter(element -> ((ScalarTree) element.key()).value().equals("resources"))
      .map(TupleTree::value)
      .filter(SequenceTree.class::isInstance)
      .map(SequenceTree.class::cast)
      .findFirst();
  }

  private static List<ResourceDeclaration> convertResources(SequenceTree resource) {
    return resource.elements().stream()
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .map(ArmParser::convertToResourceDeclaration)
      .collect(Collectors.toList());
  }

  private static ResourceDeclaration convertToResourceDeclaration(MappingTree tree) {
    Property type = null;
    Property version = null;
    Property name = null;
    List<Property> otherProperties = new ArrayList<>();

    for (TupleTree tuple : tree.elements()) {
      Property property = convertTupleToProperty(tuple);
      if ("type".equals(property.key().value())) {
        type = property;
      } else if ("apiVersion".equals(property.key().value())) {
        version = property;
      } else if ("name".equals(property.key().value())) {
        name = property;
      } else {
        otherProperties.add(property);
      }
    }

    if (type == null || version == null || name == null) {
      throw new ParseException("Resource without required field spotted (name, type, apiVersion)", null, null);
    }

    return new ResourceDeclarationImpl(name, version, type, otherProperties);
  }

  private static Property convertTupleToProperty(TupleTree tuple) {
    return new PropertyImpl(convertToIdentifier(tuple.key()), convertToExpression(tuple.value()));
  }

  private static Identifier convertToIdentifier(YamlTree tree) {
    return Optional.of(tree)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(scalarTree -> new IdentifierImpl(scalarTree.value(), scalarTree.metadata()))
      .orElseThrow(
        () -> new ParseException("Expecting ScalarTree to convert to Identifier, got " + tree.getClass().getSimpleName(), null, null));
  }

  private static Expression convertToExpression(YamlTree tree) {
    return Optional.of(tree)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(scalarTree -> new ExpressionImpl(scalarTree.value(), scalarTree.metadata()))
      .orElseThrow(
        () -> new ParseException("Expecting ScalarTree to convert to Expression, got " + tree.getClass().getSimpleName(), null, null));
  }
}
