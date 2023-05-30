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
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.PropertyValue;
import org.sonar.iac.arm.tree.api.SimpleProperty;
import org.sonar.iac.arm.tree.impl.json.ArrayExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.ExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.json.ObjectExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.PropertyImpl;
import org.sonar.iac.arm.tree.impl.json.SimplePropertyImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class ArmConverter {

  private static final Logger LOG = Loggers.get(ArmConverter.class);
  @Nullable
  protected final InputFileContext inputFileContext;

  public ArmConverter(@Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
  }

  public Map<String, Property> extractProperties(MappingTree tree) {
    return convertToObjectExpression(tree).getMapRepresentation();
  }

  public Identifier convertToIdentifier(YamlTree tree) {
    return Optional.of(tree)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(scalarTree -> new IdentifierImpl(scalarTree.value(), scalarTree.metadata()))
      .orElseThrow(
        () -> new ParseException("Expecting ScalarTree to convert to Identifier, got " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null));
  }

  public Expression convertToExpression(YamlTree tree) {
    return Optional.of(tree)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(scalarTree -> new ExpressionImpl(scalarTree.value(), scalarTree.metadata()))
      .orElseThrow(
        () -> new ParseException("Expecting ScalarTree to convert to Expression, got " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null));
  }

  public ObjectExpression convertToObjectExpression(MappingTree tree) {
    List<Property> properties = new ArrayList<>();
    tree.elements()
      .forEach(tupleTree -> {
        Identifier key = convertToIdentifier(tupleTree.key());
        PropertyValue value = convertToPropertyValue(tupleTree.value());
        properties.add(new PropertyImpl(key, value));
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
      return convertToExpression(tree);
    } else {
      throw new ParseException("Couldn't convert to PropertyValue, unsupported class " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null);
    }
  }

  public SimpleProperty extractMandatorySimpleProperty(YamlTreeMetadata metadata, Map<String, Property> properties, String key) {
    Property value = properties.remove(key);
    if (value == null) {
      throw new ParseException("Missing mandatory attribute '" + key + "' at " + buildLocation(metadata.textRange()), new BasicTextPointer(metadata.textRange()), null);
    }
    return convertToSimpleProperty(value);
  }

  public SimpleProperty extractSimpleProperty(Map<String, Property> properties, String key) {
    Property value = properties.remove(key);
    if (value == null) {
      return null;
    }
    return convertToSimpleProperty(value);
  }

  public Property extractProperty(Map<String, Property> properties, String key) {
    return properties.remove(key);
  }

  public ArrayExpression extractArrayExpression(Map<String, Property> properties, String key) {
    Property value = properties.remove(key);
    if (value == null) {
      return null;
    }
    if (!(value.value() instanceof ArrayExpression)) {
      throw new ParseException("extractArrayExpression: Expecting ArrayExpression in property value, got " + value.value().getClass().getSimpleName() + " instead at " +
        buildLocation(value.value().textRange()), new BasicTextPointer(value.value().textRange()), null);
    }
    return (ArrayExpression) value.value();
  }

  @CheckForNull
  public SimpleProperty convertToSimpleProperty(@Nullable Property property) {
    if (property == null) {
      return null;
    }
    if (!property.value().is(ArmTree.Kind.EXPRESSION)) {
      throw new ParseException("convertToSimpleProperty: Expecting Expression in property value, got " + property.value().getClass().getSimpleName() + " instead at " +
        buildLocation(property.value().textRange()), new BasicTextPointer(property.value().textRange()), null);
    }
    return new SimplePropertyImpl(property.key(), (Expression) property.value());
  }

  public void checkUnexpectedProperties(Map<String, Property> byKeys, String id) {
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
    if (!propertyValue.is(ArmTree.Kind.EXPRESSION)) {
      throw new ParseException("toExpression: Expecting Expression, got " + propertyValue.getClass().getSimpleName() + " instead at " +
        buildLocation(propertyValue.textRange()), new BasicTextPointer(propertyValue.textRange()), null);
    }
    return (Expression) propertyValue;
  }

  public ObjectExpression toObjectExpression(PropertyValue propertyValue) {
    if (!propertyValue.is(ArmTree.Kind.OBJECT_EXPRESSION)) {
      throw new ParseException("toObjectExpression: Expecting ObjectExpression, got " + propertyValue.getClass().getSimpleName() + " instead at " +
        buildLocation(propertyValue.textRange()), new BasicTextPointer(propertyValue.textRange()), null);
    }
    return (ObjectExpression) propertyValue;
  }

  // Log related methods
  public String buildLocation(TextRange position) {
    String filename = "";
    if (inputFileContext != null) {
      filename = inputFileContext.inputFile.filename() + ":";
    }
    return filename + position.start().line() + ":" + position.start().lineOffset();
  }

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
