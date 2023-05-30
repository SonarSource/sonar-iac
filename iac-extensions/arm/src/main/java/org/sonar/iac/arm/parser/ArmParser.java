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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.internal.apachecommons.lang.StringUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.PropertyValue;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.SimpleProperty;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.impl.json.ArrayExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.ExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.FileImpl;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.json.ObjectExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.OutputDeclarationImpl;
import org.sonar.iac.arm.tree.impl.json.ParameterDeclarationImpl;
import org.sonar.iac.arm.tree.impl.json.PropertyImpl;
import org.sonar.iac.arm.tree.impl.json.ResourceDeclarationImpl;
import org.sonar.iac.arm.tree.impl.json.SimplePropertyImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.MappingTreeImpl;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class ArmParser implements TreeParser<ArmTree> {

  private static final Logger LOG = Loggers.get(ArmParser.class);
  private static final List<String> EXPECTED_KEYS_IN_PARAMETERS = List.of("allowedValues", "metadata", "type", "defaultValue", "minValue", "maxValue", "minLength", "maxLength");
  @Nullable
  private InputFileContext inputFileContext;

  @Override
  public ArmTree parse(String source, @Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
    return convert(parseJson(source));
  }

  private FileTree parseJson(String source) {
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

  private ArmTree convert(FileTree fileTree) {
    List<Statement> statements = new ArrayList<>();
    MappingTree document = (MappingTree) fileTree.documents().get(0);

    extractResourcesSequence(document).ifPresent(sequence -> {
      List<ResourceDeclaration> resourceDeclarations = convertResources(sequence);
      statements.addAll(resourceDeclarations);
    });

    extractOutputsMapping(document).ifPresent(mapping -> {
      List<OutputDeclaration> outputDeclarations = convertOutputsDeclaration(mapping);
      statements.addAll(outputDeclarations);
    });

    List<Statement> params = extractParametersSequence(fileTree)
      .map(this::convertParameters)
      .collect(Collectors.toList());
    statements.addAll(params);

    return new FileImpl(statements);
  }

  private static Stream<TupleTree> extractParametersSequence(FileTree fileTree) {
    MappingTree document = (MappingTree) fileTree.documents().get(0);
    return document.elements().stream()
      .filter(element -> element.key() instanceof ScalarTree)
      .filter(element -> "parameters".equals(((ScalarTree) element.key()).value()))
      .map(TupleTree::value)
      .map(MappingTree.class::cast)
      .map(MappingTree::elements)
      .flatMap(List::stream);
  }

  private ParameterDeclaration convertParameters(TupleTree tupleTree) {
    String id = ((ScalarTree) tupleTree.key()).value();
    Identifier identifier = new IdentifierImpl(id, tupleTree.key().metadata());

    List<TupleTree> elements = ((MappingTreeImpl) tupleTree.value()).elements();
    Map<String, TupleTree> byKeys = associateByKey(elements);
    SimpleProperty type = extractParameterByKey(byKeys, "type");
    if (type == null) {
      String fileAndPosition = filenameAndPosition(tupleTree.metadata().textRange());
      String message = String.format("Missing required field 'type' in Parameter %s at %s", id, fileAndPosition);
      throw new ParseException(message, null, null);
    }

    SimpleProperty defaultValue = extractParameterByKey(byKeys, "defaultValue");
    SimpleProperty minValue = extractParameterByKey(byKeys, "minValue");
    SimpleProperty maxValue = extractParameterByKey(byKeys, "maxValue");
    SimpleProperty minLength = extractParameterByKey(byKeys, "minLength");
    SimpleProperty maxLength = extractParameterByKey(byKeys, "maxLength");
    List<Expression> allowedValues = extractParameterAllowedValues(byKeys);
    SimpleProperty description = extractParameterDescription(byKeys);

    checkUnexpectedProperties(byKeys, id);

    return new ParameterDeclarationImpl(
      identifier,
      type,
      defaultValue,
      allowedValues,
      description,
      minValue,
      maxValue,
      minLength,
      maxLength);
  }

  private static SimpleProperty extractParameterByKey(Map<String, TupleTree> byKeys, String name) {
    SimpleProperty result = null;
    TupleTree typeTuple = byKeys.get(name);
    if (typeTuple != null) {
      try {
        result = convertTupleToSimpleProperty(typeTuple);
      } catch (ParseException e) {
        // TODO SONARIAC-841 please remove this try/catch when all types of defaultValue will be supported
        LOG.debug("Unsupported type of defaultValue, ignoring it");
      }
    }
    return result;
  }

  private static List<Expression> extractParameterAllowedValues(Map<String, TupleTree> byKeys) {
    List<Expression> allowedValues = Collections.emptyList();
    TupleTree tupleTree = byKeys.get("allowedValues");
    if (tupleTree != null) {
      allowedValues = ((SequenceTree) tupleTree.value()).elements().stream()
        .map(ScalarTree.class::cast)
        .map(allowedVal -> new ExpressionImpl(allowedVal.value(), allowedVal.metadata()))
        .collect(Collectors.toList());
    }
    return allowedValues;
  }

  private static SimpleProperty extractParameterDescription(Map<String, TupleTree> byKeys) {
    SimpleProperty description = null;
    TupleTree tupleTree = byKeys.get("metadata");
    if (tupleTree != null) {
      description = ((MappingTree) tupleTree.value()).elements().stream()
        .filter(tuple -> "description".equals(((ScalarTree) tuple.key()).value()))
        .map(ArmParser::convertTupleToSimpleProperty)
        .findFirst()
        .orElse(null);
    }
    return description;
  }

  private static Map<String, TupleTree> associateByKey(Collection<TupleTree> tuples) {
    Map<String, TupleTree> properties = new HashMap<>();
    for (TupleTree tuple : tuples) {
      String tupleKey = ((ScalarTree) tuple.key()).value();
      properties.put(tupleKey, tuple);
    }
    return properties;
  }

  private void checkUnexpectedProperties(Map<String, TupleTree> byKeys, String id) {
    byKeys.entrySet().stream()
      .filter(element -> !EXPECTED_KEYS_IN_PARAMETERS.contains(element.getKey()))
      .forEach(element -> {
        String fileAndPosition = filenameAndPosition(element.getValue().textRange());
        LOG.debug("Unexpected property `{}` found in parameter {} at {}, ignoring it.", element.getKey(), id, fileAndPosition);
      });
  }

  private String filenameAndPosition(TextRange textRange) {
    String position = textRange.start().line() + ":" + textRange.start().lineOffset();
    if (inputFileContext != null) {
      String filename = inputFileContext.inputFile.filename();
      if (StringUtils.isNotBlank(filename)) {
        return filename + ":" + position;
      }
    }
    return position;
  }

  private static Optional<SequenceTree> extractResourcesSequence(MappingTree document) {
    return document.elements().stream()
      .filter(element -> element.key() instanceof ScalarTree)
      .filter(element -> "resources".equals(((ScalarTree) element.key()).value()))
      .map(TupleTree::value)
      .filter(SequenceTree.class::isInstance)
      .map(SequenceTree.class::cast)
      .findFirst();
  }

  private static Optional<MappingTree> extractOutputsMapping(MappingTree document) {
    return document.elements().stream()
      .filter(element -> element.key() instanceof ScalarTree)
      .filter(element -> "outputs".equals(((ScalarTree) element.key()).value()))
      .map(TupleTree::value)
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .findFirst();
  }

  private List<ResourceDeclaration> convertResources(SequenceTree resource) {
    return resource.elements().stream()
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .map(this::convertToResourceDeclaration)
      .collect(Collectors.toList());
  }

  private List<OutputDeclaration> convertOutputsDeclaration(MappingTree mapping) {
    return mapping.elements().stream()
      .map(this::convertToOutputDeclaration)
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

  private OutputDeclaration convertToOutputDeclaration(TupleTree tupleTree) {
    Identifier name = convertToIdentifier(tupleTree.key());

    MappingTree outputMapping = toMappingTree(tupleTree.value());
    Map<String, Property> properties = extractProperties(outputMapping);

    SimpleProperty type = extractMandatorySimpleProperty(tupleTree.metadata(), properties, "type");
    SimpleProperty condition = extractSimpleProperty(properties, "condition");
    SimpleProperty value = extractSimpleProperty(properties, "value");
    SimpleProperty copyCount = null;
    SimpleProperty copyInput = null;

    if (properties.containsKey("copy")) {
      ObjectExpression copy = toObjectExpression(properties.remove("copy").value());
      copyCount = convertToSimpleProperty(copy.getPropertyByName("count"));
      copyInput = convertToSimpleProperty(copy.getPropertyByName("input"));
    }

    for (Map.Entry<String, Property> unexpectedProperty : properties.entrySet()) {
      TextRange position = unexpectedProperty.getValue().textRange();
      LOG.debug("Unexpected property '{}' found in output declaration at {}, ignoring it.", unexpectedProperty.getKey(), buildLocation(position));
    }

    return new OutputDeclarationImpl(name, type, condition, copyCount, copyInput, value);
  }

  private String buildLocation(TextRange position) {
    String filename = "";
    if (inputFileContext != null) {
      filename = inputFileContext.inputFile.filename() + ":";
    }
    return filename + position.start().line() + ":" + position.start().lineOffset();
  }

  private static MappingTree toMappingTree(YamlTree tree) {
    if (!(tree instanceof MappingTree)) {
      throw new ParseException("Expected MappingTree, got " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null);
    }
    return (MappingTree) tree;
  }

  private static Map<String, Property> extractProperties(MappingTree tree) {
    return convertToObjectExpression(tree).getMapRepresentation();
  }

  private static SimpleProperty convertTupleToSimpleProperty(TupleTree tuple) {
    return new SimplePropertyImpl(convertToIdentifier(tuple.key()), convertToExpression(tuple.value()));
  }

  private static Identifier convertToIdentifier(YamlTree tree) {
    return Optional.of(tree)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(scalarTree -> new IdentifierImpl(scalarTree.value(), scalarTree.metadata()))
      .orElseThrow(
        () -> new ParseException("Expecting ScalarTree to convert to Identifier, got " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null));
  }

  private static Expression convertToExpression(YamlTree tree) {
    return Optional.of(tree)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(scalarTree -> new ExpressionImpl(scalarTree.value(), scalarTree.metadata()))
      .orElseThrow(
        () -> new ParseException("Expecting ScalarTree to convert to Expression, got " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null));
  }

  private static ArrayExpression convertToArrayExpression(SequenceTree tree) {
    return new ArrayExpressionImpl(tree.metadata(),
      tree.elements().stream()
        .map(ArmParser::convertToPropertyValue)
        .collect(Collectors.toList()));
  }

  private static ObjectExpression convertToObjectExpression(MappingTree tree) {
    List<Property> properties = new ArrayList<>();
    tree.elements()
      .forEach(tupleTree -> {
        Identifier key = convertToIdentifier(tupleTree.key());
        PropertyValue value = convertToPropertyValue(tupleTree.value());
        properties.add(new PropertyImpl(key, value));
      });
    return new ObjectExpressionImpl(properties);
  }

  private static PropertyValue convertToPropertyValue(YamlTree tree) {
    if (tree instanceof SequenceTree) {
      return convertToArrayExpression((SequenceTree) tree);
    } else if (tree instanceof MappingTree) {
      return convertToObjectExpression((MappingTree) tree);
    } else if (tree instanceof ScalarTree) {
      return convertToExpression(tree);
    } else {
      throw new ParseException("Couldn't convert to PropertyValue, unsupported class " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null);
    }
  }

  private SimpleProperty extractMandatorySimpleProperty(YamlTreeMetadata metadata, Map<String, Property> properties, String key) {
    Property value = properties.remove(key);
    if (value == null) {
      throw new ParseException("Missing mandatory attribute '" + key + "' at " + buildLocation(metadata.textRange()), new BasicTextPointer(metadata.textRange()), null);
    }
    return convertToSimpleProperty(value);
  }

  private SimpleProperty extractSimpleProperty(Map<String, Property> properties, String key) {
    Property value = properties.remove(key);
    if (value == null) {
      return null;
    }
    return convertToSimpleProperty(value);
  }

  @CheckForNull
  private SimpleProperty convertToSimpleProperty(@Nullable Property property) {
    if (property == null) {
      return null;
    }
    if (!property.value().is(ArmTree.Kind.EXPRESSION)) {
      throw new ParseException("convertToSimpleProperty: Expecting Expression in property value, got " + property.value().getClass().getSimpleName() + " instead at " +
        buildLocation(property.value().textRange()), new BasicTextPointer(property.value().textRange()), null);
    }
    return new SimplePropertyImpl(property.key(), (Expression) property.value());
  }

  private ObjectExpression toObjectExpression(PropertyValue propertyValue) {
    if (!propertyValue.is(ArmTree.Kind.OBJECT_EXPRESSION)) {
      throw new ParseException("toObjectExpression: Expecting ObjectExpression, got " + propertyValue.getClass().getSimpleName() + " instead at " +
        buildLocation(propertyValue.textRange()), new BasicTextPointer(propertyValue.textRange()), null);
    }
    return (ObjectExpression) propertyValue;
  }
}
