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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.api.internal.apachecommons.lang.StringUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.PropertyValue;
import org.sonar.iac.arm.tree.impl.json.ArrayExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.BooleanLiteralImpl;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.json.NullLiteralImpl;
import org.sonar.iac.arm.tree.impl.json.NumericLiteralImpl;
import org.sonar.iac.arm.tree.impl.json.ObjectExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.PropertyImpl;
import org.sonar.iac.arm.tree.impl.json.StringLiteralImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class ArmBaseConverter {

  private static final Logger LOG = Loggers.get(ArmBaseConverter.class);
  @Nullable
  protected final InputFileContext inputFileContext;

  public ArmBaseConverter(@Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
  }

  public Map<String, Property<PropertyValue>> extractProperties(MappingTree tree) {
    return convertToObjectExpression(tree).getMapRepresentation();
  }

  protected Predicate<TupleTree> filterOnField(String field) {
    return tupleTree -> tupleTree.key() instanceof ScalarTree && field.equals(((ScalarTree) tupleTree.key()).value());
  }

  // Convert methods
  public Identifier convertToIdentifier(YamlTree tree) {
    return Optional.of(tree)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(scalarTree -> new IdentifierImpl(scalarTree.value(), scalarTree.metadata()))
      .orElseThrow(
        () -> new ParseException("Expecting ScalarTree to convert to Identifier, got " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null));
  }

  public Expression convertToExpression(ScalarTree tree) {
    if (tree.style() == ScalarTree.Style.PLAIN) {
      if ("null".equals(tree.value())) {
        return new NullLiteralImpl(tree.metadata());
      } else if ("true".equals(tree.value()) || "false".equals(tree.value())) {
        return new BooleanLiteralImpl("true".equals(tree.value()), tree.metadata());
      } else {
        return new NumericLiteralImpl(Double.parseDouble(tree.value()), tree.metadata());
      }
    } else if (tree.style() == ScalarTree.Style.DOUBLE_QUOTED) {
      return new StringLiteralImpl(tree.value(), tree.metadata());
    } else {
      throw new ParseException("Unsupported ScalarTree style: " + tree.style().name(), new BasicTextPointer(tree.metadata().textRange()), null);
    }
  }

  public ObjectExpression convertToObjectExpression(MappingTree tree) {
    List<Property<PropertyValue>> properties = new ArrayList<>();
    tree.elements()
      .forEach(tupleTree -> {
        Identifier key = convertToIdentifier(tupleTree.key());
        PropertyValue value = convertToPropertyValue(tupleTree.value());
        properties.add(new PropertyImpl<>(key, value));
      });
    return new ObjectExpressionImpl(properties);
  }

  public ArrayExpression convertToArrayExpression(SequenceTree tree) {
    return new ArrayExpressionImpl(tree.metadata(),
      tree.elements().stream()
        .map(this::convertToPropertyValue)
        .collect(Collectors.toList()));
  }

  public PropertyValue convertToPropertyValue(YamlTree tree) {
    if (tree instanceof SequenceTree) {
      return convertToArrayExpression((SequenceTree) tree);
    } else if (tree instanceof MappingTree) {
      return convertToObjectExpression((MappingTree) tree);
    } else if (tree instanceof ScalarTree) {
      return convertToExpression((ScalarTree) tree);
    } else {
      throw new ParseException("Couldn't convert to PropertyValue, unsupported class " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null);
    }
  }

  // Extract methods
  @SuppressWarnings("unchecked")
  public <T extends PropertyValue> Property<T> extractMandatoryProperty(YamlTreeMetadata metadata, Map<String, Property<PropertyValue>> properties, String key, ArmTree.Kind kind) {
    Property<PropertyValue> value = properties.remove(key);
    if (value == null) {
      throw new ParseException("Missing mandatory attribute '" + key + "' at " + filenameAndPosition(metadata.textRange()), new BasicTextPointer(metadata.textRange()), null);
    }
    throwErrorIfUnexpectedType("Fail to extract mandatory Property '" + key + "'", value.value(), kind);
    return (Property<T>) value;
  }

  @SuppressWarnings("unchecked")
  public <T extends PropertyValue> Property<T> extractProperty(Map<String, Property<PropertyValue>> properties, String key, ArmTree.Kind... kinds) {
    Property<PropertyValue> value = properties.remove(key);
    if (value == null) {
      return null;
    }
    throwErrorIfUnexpectedType("Fail to extract Property '" + key + "'", value.value(), kinds);
    return (Property<T>) value;
  }

  @SuppressWarnings("unchecked")
  public <T extends PropertyValue> Property<T> toProperty(@Nullable Property<PropertyValue> property, ArmTree.Kind kind) {
    if (property == null) {
      return null;
    }
    throwErrorIfUnexpectedType("Fail to cast to Property", property.value(), kind);
    return (Property<T>) property;
  }

  public ArrayExpression extractArrayExpression(Map<String, Property<PropertyValue>> properties, String key) {
    Property<PropertyValue> value = properties.remove(key);
    if (value == null) {
      return null;
    }
    throwErrorIfUnexpectedType("Fail to extract ArrayExpression", value.value(), ArmTree.Kind.ARRAY_EXPRESSION);
    return (ArrayExpression) value.value();
  }

  public void checkUnexpectedProperties(Map<String, Property<PropertyValue>> byKeys, String id) {
    byKeys.forEach((key, value) -> {
      String fileAndPosition = filenameAndPosition(value.textRange());
      LOG.debug("Unexpected property `{}` found in parameter {} at {}, ignoring it.", key, id, fileAndPosition);
    });
  }

  // Cast methods
  public MappingTree toMappingTree(YamlTree tree) {
    if (!(tree instanceof MappingTree)) {
      throw new ParseException("Expected MappingTree, got " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null);
    }
    return (MappingTree) tree;
  }

  public Expression toExpression(PropertyValue propertyValue) {
    throwErrorIfUnexpectedType("Fail to cast to Expression", propertyValue, ArmTree.Kind.STRING_LITERAL, ArmTree.Kind.NUMERIC_LITERAL, ArmTree.Kind.NULL_LITERAL,
      ArmTree.Kind.BOOLEAN_LITERAL);
    return (Expression) propertyValue;
  }

  public ObjectExpression toObjectExpression(PropertyValue propertyValue) {
    throwErrorIfUnexpectedType("Fail to Cast to ObjectExpression", propertyValue, ArmTree.Kind.OBJECT_EXPRESSION);
    return (ObjectExpression) propertyValue;
  }

  protected void throwErrorIfUnexpectedType(String method, ArmTree object, ArmTree.Kind... expectedKinds) {
    for (ArmTree.Kind expectedKind : expectedKinds) {
      if (object.is(expectedKind)) {
        return;
      }
    }
    String expected = "[" + StringUtils.join(Arrays.stream(expectedKinds).map(kind -> kind.getAssociatedInterface().getSimpleName()).collect(Collectors.toList()), ", ") + "]";
    throw new ParseException(method + ": Expecting " + expected + ", got " + object.getClass().getSimpleName()
      + " instead at " + filenameAndPosition(object.textRange()), new BasicTextPointer(object.textRange()), null);
  }

  // Log related methods
  public String filenameAndPosition(TextRange textRange) {
    String position = textRange.start().line() + ":" + textRange.start().lineOffset();
    if (inputFileContext != null) {
      String filename = inputFileContext.inputFile.filename();
      if (StringUtils.isNotBlank(filename)) {
        return filename + ":" + position;
      }
    }
    return position;
  }
}
