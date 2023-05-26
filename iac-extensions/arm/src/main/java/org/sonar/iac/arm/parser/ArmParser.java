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
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.impl.json.ExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.FileImpl;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.json.OutputDeclarationImpl;
import org.sonar.iac.arm.tree.impl.json.PropertyImpl;
import org.sonar.iac.arm.tree.impl.json.ResourceDeclarationImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.impl.json.FileImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.arm.tree.impl.json.ParameterDeclarationImpl;
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
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;
import org.sonar.iac.common.yaml.tree.MappingTreeImpl;

public class ArmParser implements TreeParser<ArmTree> {

  private static final Logger LOG = Loggers.get(ArmParser.class);
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

    extractResourcesSequence(fileTree).ifPresent(res -> statements.addAll(convertResources(res)));

    extractOutputsMapping(document).ifPresent(mapping -> {
      List<OutputDeclaration> outputDeclarations = convertOutputsDeclaration(mapping);
      statements.addAll(outputDeclarations);
    });

    List<Statement> params = extractParametersSequence(fileTree)
      .map(parameters -> convertParameters(parameters, filename))
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

  private static ParameterDeclaration convertParameters(TupleTree tupleTree, String filename) {
    String id = ((ScalarTree) tupleTree.key()).value();
    Identifier identifier = new IdentifierImpl(id, tupleTree.key().metadata());

    Property type = null;
    Expression defaultValue = null;
    ExpressionImpl minValue = null;
    ExpressionImpl maxValue = null;
    ExpressionImpl minLength = null;
    ExpressionImpl maxLength = null;
    List<Expression> allowedValues = Collections.emptyList();
    ExpressionImpl description = null;
    List<TupleTree> elements = ((MappingTreeImpl) tupleTree.value()).elements();
    for (TupleTree element : elements) {
      String keyName = ((ScalarTree) element.key()).value();
      switch (keyName) {
        case "type":
          Identifier key = new IdentifierImpl(keyName, element.key().metadata());
          Expression value = new ExpressionImpl(((ScalarTree) element.value()).value(), element.value().metadata());
          type = new PropertyImpl(key, value);
          break;

        case "defaultValue":
          String defValue = ((ScalarTree) element.value()).value();
          defaultValue = new ExpressionImpl(defValue, element.value().metadata());
          break;

        case "minValue":
          String minValueText = ((ScalarTree) element.value()).value();
          minValue = new ExpressionImpl(minValueText, element.value().metadata());
          break;

        case "maxValue":
          String maxValueText = ((ScalarTree) element.value()).value();
          maxValue = new ExpressionImpl(maxValueText, element.value().metadata());
          break;

        case "minLength":
          String minLengthText = ((ScalarTree) element.value()).value();
          minLength = new ExpressionImpl(minLengthText, element.value().metadata());
          break;

        case "maxLength":
          String maxLengthText = ((ScalarTree) element.value()).value();
          maxLength = new ExpressionImpl(maxLengthText, element.value().metadata());
          break;

        case "allowedValues":
          allowedValues = ((SequenceTree) element.value()).elements().stream()
            .map(ScalarTree.class::cast)
            .map(allowedVal -> new ExpressionImpl(allowedVal.value(), allowedVal.metadata()))
            .collect(Collectors.toList());
          break;

        case "metadata":
          description = ((MappingTree) element.value()).elements().stream()
            .filter(a -> "description".equals(((ScalarTree) a.key()).value()))
            .map(a -> new ExpressionImpl(((ScalarTree) a.value()).value(), a.value().metadata()))
            .findFirst()
            .orElse(null);
          break;

        default:
          String fileAndPosition = filenameAndPosition(filename, element.metadata().textRange());
          LOG.debug("Unexpected property `{}` found in parameter {} at {}, ignoring it.", keyName, id, fileAndPosition);
      }
    }
    if (type == null) {
      String fileAndPosition = filenameAndPosition(filename, tupleTree.metadata().textRange());
      String message = String.format("Missing required field 'type' in Parameter %s at %s", id, fileAndPosition);
      throw new ParseException(message, null, null);
    }
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

  private static String filenameAndPosition(String filename, TextRange textRange) {
    String position = textRange.start().line() + ":" + textRange.start().lineOffset();
    if (StringUtils.isNotBlank(filename)) {
      return filename + ":" + position;
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
      .filter(element -> ((ScalarTree) element.key()).value().equals("outputs"))
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
    Map<String, Property> properties = extractProperties(tree.elements());

    Property type = properties.remove("type");
    Property version = properties.remove("apiVersion");
    Property name = properties.remove("name");
    List<Property> otherProperties = new ArrayList<>(properties.values());

    checkMandatoryObject(tree.metadata(), "type", type, "apiVersion", version, "name", name);

    return new ResourceDeclarationImpl(name, version, type, otherProperties);
  }

  private OutputDeclaration convertToOutputDeclaration(TupleTree tupleTree) {
    Identifier name = convertToIdentifier(tupleTree.key());

    MappingTree outputMapping = toMappingTree(tupleTree.value());
    Map<String, Property> properties = extractProperties(outputMapping.elements());

    Property type = properties.remove("type");
    Property condition = properties.remove("condition");
    Property copyCount = properties.remove("copy.count");
    Property copyInput = properties.remove("copy.input");
    Property value = properties.remove("value");

    for (Map.Entry<String, Property> unexpectedProperty : properties.entrySet()) {
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
  private static Map<String, Property> extractProperties(Collection<TupleTree> tuples) {
    Map<String, Property> properties = new HashMap<>();
    return extractProperties(properties, "", tuples);
  }

  private static Map<String, Property> extractProperties(Map<String, Property> properties, String prefix, Collection<TupleTree> tuples) {
    for (TupleTree tuple : tuples) {
      String tupleKey = ((ScalarTree) tuple.key()).value();
      YamlTree tupleValue = tuple.value();
      if (tupleValue instanceof MappingTree) {
        MappingTree mappingTree = (MappingTree) tupleValue;
        extractProperties(properties, prefix + tupleKey + ".", mappingTree.elements());
      } else if (tupleValue instanceof ScalarTree) {
        properties.put(prefix + tupleKey, convertTupleToProperty(tuple));
      } else {
        throw new ParseException("Unsupported type for extractProperties, expected MappingTree or ScalarTree, got '" + tupleValue.getClass().getSimpleName() + "'",
          new BasicTextPointer(tupleValue.metadata().textRange()), null);
      }
    }

    return properties;
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
}
