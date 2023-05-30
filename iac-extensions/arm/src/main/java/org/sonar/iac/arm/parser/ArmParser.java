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
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.TextPointer;
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
    Property type = extractParameterByKey(byKeys, "type");
    if (type == null) {
      String fileAndPosition = filenameAndPosition(tupleTree.metadata().textRange());
      String message = String.format("Missing required field 'type' in Parameter %s at %s", id, fileAndPosition);
      throw new ParseException(message, null, null);
    }

    Property defaultValue = extractParameterByKey(byKeys, "defaultValue");
    Property minValue = extractParameterByKey(byKeys, "minValue");
    Property maxValue = extractParameterByKey(byKeys, "maxValue");
    Property minLength = extractParameterByKey(byKeys, "minLength");
    Property maxLength = extractParameterByKey(byKeys, "maxLength");
    List<Expression> allowedValues = extractParameterAllowedValues(byKeys);
    Property description = extractParameterDescription(byKeys);

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

  private static Property extractParameterByKey(Map<String, TupleTree> byKeys, String name) {
    Property result = null;
    TupleTree typeTuple = byKeys.get(name);
    if (typeTuple != null) {
      try {
        result = convertTupleToProperty(typeTuple);
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

  private static Property extractParameterDescription(Map<String, TupleTree> byKeys) {
    Property description = null;
    TupleTree tupleTree = byKeys.get("metadata");
    if (tupleTree != null) {
      description = ((MappingTree) tupleTree.value()).elements().stream()
        .filter(tuple -> "description".equals(((ScalarTree) tuple.key()).value()))
        .map(ArmParser::convertTupleToProperty)
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

  private static List<ResourceDeclaration> convertResources(SequenceTree resource) {
    return resource.elements().stream()
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .map(ArmParser::convertToResourceDeclaration)
      .collect(Collectors.toList());
  }

  private List<OutputDeclaration> convertOutputsDeclaration(MappingTree mapping) {
    return mapping.elements().stream()
      .map(this::convertToOutputDeclaration)
      .collect(Collectors.toList());
  }

  private static ResourceDeclaration convertToResourceDeclaration(MappingTree tree) {
    Map<String, SimpleProperty> properties = extractSimpleProperties(tree.elements());

    SimpleProperty type = properties.remove("type");
    SimpleProperty version = properties.remove("apiVersion");
    SimpleProperty name = properties.remove("name");
    List<Property> otherProperties = new ArrayList<>(properties.values());

    checkMandatoryObject(tree.metadata(), "type", type, "apiVersion", version, "name", name);

    return new ResourceDeclarationImpl(name, version, type, otherProperties);
  }

  private OutputDeclaration convertToOutputDeclaration(TupleTree tupleTree) {
    Identifier name = convertToIdentifier(tupleTree.key());

    MappingTree outputMapping = toMappingTree(tupleTree.value());
    Map<String, SimpleProperty> properties = extractSimpleProperties(outputMapping.elements());
    Map<String, PropertyValue> extractProperties = extractProperties(outputMapping);

    SimpleProperty type = properties.remove("type");
    SimpleProperty condition = properties.remove("condition");
    SimpleProperty copyCount = properties.remove("copy.count");
    SimpleProperty copyInput = properties.remove("copy.input");
    SimpleProperty value = properties.remove("value");

    for (Map.Entry<String, SimpleProperty> unexpectedProperty : properties.entrySet()) {
      TextRange position = unexpectedProperty.getValue().textRange();
      LOG.debug("Unexpected property '{}' found in output declaration at {}, ignoring it.", unexpectedProperty.getKey(), buildLocation(position));
    }

    checkMandatoryObject(tupleTree.metadata(), "type", type);

    return new OutputDeclarationImpl(name, type, condition, copyCount, copyInput, value);
  }

  private String buildLocation(TextRange position) {
    String filename = "";
    if (inputFileContext != null) {
      filename = inputFileContext.inputFile.filename() + ":";
    }
    return filename + position.start().line() + ":" + position.start().lineOffset();
  }

  /**
   * This method will raise a Parse exception if any of the provided object is null.
   * It requires first the metadata object to report the location in the exception.
   * Then it is expecting arguments by pair: the name of the argument (for the exception message) and the object reference to check.
   */
  private static void checkMandatoryObject(YamlTreeMetadata metadata, Object... objects) {
    List<String> missing = new ArrayList<>();
    for (int i = 0; i < objects.length - 1; i += 2) {
      String objectName = (String) objects[i];
      Object objectReference = objects[i + 1];
      if (objectReference == null) {
        missing.add(objectName);
      }
    }

    if (!missing.isEmpty()) {
      TextPointer pointer = new BasicTextPointer(metadata.textRange());
      StringBuilder errorMessage = new StringBuilder()
        .append("Missing required field")
        .append(missing.size() > 1 ? "s" : "")
        .append(" [\"").append(StringUtils.join(missing, "\", \"")).append("\"]")
        .append(" at ").append(pointer.line()).append(":").append(pointer.lineOffset());
      throw new ParseException(errorMessage.toString(), pointer, null);
    }
  }

  private static MappingTree toMappingTree(YamlTree tree) {
    if (!(tree instanceof MappingTree)) {
      throw new ParseException("Expected MappingTree, got " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null);
    }
    return (MappingTree) tree;
  }

  private static Map<String, PropertyValue> extractProperties(MappingTree tree) {
    return convertToObjectExpression(tree).properties();
  }

  /**
   * TODO: SONARIAC-840, Property value could potentially be a list of Property (or even an array), change this method behaviour to not concatenate them with a separator.
   * Transform a collection of TupleTree into a Map of Property to easily find specific properties and process them.
   * This is a recursive method which will also convert sub-object, adding a prefix at every level.
   * Example :
   * {
   *   "key1" : "value1",
   *   "key2 : {
   *     "key3" : "value3"
   *   }
   * }
   * => Will be translated into this Map {"key1":"value1", "key2.key3":"value3"}
   */
  private static Map<String, SimpleProperty> extractSimpleProperties(Collection<TupleTree> tuples) {
    Map<String, SimpleProperty> properties = new HashMap<>();
    return extractSimpleProperties(properties, "", tuples);
  }

  private static Map<String, SimpleProperty> extractSimpleProperties(Map<String, SimpleProperty> properties, String prefix, Collection<TupleTree> tuples) {
    for (TupleTree tuple : tuples) {
      String tupleKey = ((ScalarTree) tuple.key()).value();
      YamlTree tupleValue = tuple.value();
      if (tupleValue instanceof MappingTree) {
        MappingTree mappingTree = (MappingTree) tupleValue;
        extractSimpleProperties(properties, prefix + tupleKey + ".", mappingTree.elements());
      } else if (tupleValue instanceof ScalarTree) {
        properties.put(prefix + tupleKey, convertTupleToSimpleProperty(tuple));
      } else {
        throw new ParseException("Unsupported type for extractProperties, expected MappingTree or ScalarTree, got '" + tupleValue.getClass().getSimpleName() + "'",
          new BasicTextPointer(tupleValue.metadata().textRange()), null);
      }
    }

    return properties;
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
    return new ArrayExpressionImpl(
      tree.elements().stream()
      .map(ArmParser::convertToPropertyValue)
      .collect(Collectors.toList())
    );
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
      throw new ParseException("Coldn't convert to PropertyValue, unsupported class " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null)
    }
  }

  private static SimpleProperty extractSimpleProperty(Map<String, Property> properties, String key) {
    Property value = properties.remove(key);
    if (!value.value().is(ArmTree.Kind.EXPRESSION)) {
      throw new ParseException("Expecting Expression as value, got " + value.getClass().getSimpleName() + " instead", new BasicTextPointer(value.textRange()), null);
    }
    return new SimplePropertyImpl(value.key(), (Expression) value.value());
  }
}
