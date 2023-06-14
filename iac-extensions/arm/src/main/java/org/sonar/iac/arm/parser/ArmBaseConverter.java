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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.json.ArrayExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.BooleanLiteralImpl;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.json.NullLiteralImpl;
import org.sonar.iac.arm.tree.impl.json.NumericLiteralImpl;
import org.sonar.iac.arm.tree.impl.json.ObjectExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.PropertyImpl;
import org.sonar.iac.arm.tree.impl.json.StringLiteralImpl;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class ArmBaseConverter {

  @Nullable
  protected final InputFileContext inputFileContext;

  public ArmBaseConverter(@Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
  }

  public StringLiteral toStringLiteral(PropertyTree property) {
    if (!(property.value() instanceof ScalarTree)) {
      throw convertError(property, StringLiteral.class.getSimpleName(), ScalarTree.class.getSimpleName());
    }
    ScalarTree value = (ScalarTree) property.value();
    if (value.style() != ScalarTree.Style.DOUBLE_QUOTED) {
      throw convertError(property, value, StringLiteral.class.getSimpleName(), "ScalarTree.Style.DOUBLE_QUOTED");
    }
    return new StringLiteralImpl(value.value(), value.metadata());
  }

  public NumericLiteral toNumericLiteral(PropertyTree property) {
    if (!(property.value() instanceof ScalarTree)) {
      throw convertError(property, NumericLiteral.class.getSimpleName(), ScalarTree.class.getSimpleName());
    }
    ScalarTree value = (ScalarTree) property.value();
    if (value.style() != ScalarTree.Style.PLAIN) {
      throw convertError(property, value, NumericLiteral.class.getSimpleName(), "ScalarTree.Style.PLAIN");
    }
    try {
      return new NumericLiteralImpl(Float.parseFloat(value.value()), value.metadata());
    } catch (NumberFormatException e) {
      throw new ParseException("Failed to parse float value '" + value.value() + "' at " + filenameAndPosition(value.textRange()), new BasicTextPointer(value.textRange()), null);
    }
  }

  public ArrayExpression toArrayExpression(PropertyTree property) {
    if (!(property.value() instanceof SequenceTree)) {
      throw convertError(property, ArrayExpression.class.getSimpleName(), SequenceTree.class.getSimpleName());
    }
    return toArrayExpression((SequenceTree) property.value());
  }

  public Identifier toIdentifier(YamlTree tree) {
    if (!(tree instanceof ScalarTree)) {
      throw convertError(tree, Identifier.class.getSimpleName(), ScalarTree.class.getSimpleName());
    }
    ScalarTree scalarTree = (ScalarTree) tree;
    return new IdentifierImpl(scalarTree.value(), scalarTree.metadata());
  }

  public ObjectExpression toObjectExpression(MappingTree tree) {
    List<Property> properties = new ArrayList<>();
    tree.elements()
      .forEach(tupleTree -> {
        Identifier key = toIdentifier(tupleTree.key());
        Expression value = toExpression(tupleTree.value());
        properties.add(new PropertyImpl(key, value));
      });
    return new ObjectExpressionImpl(properties);
  }

  public ArrayExpression toArrayExpression(SequenceTree tree) {
    return new ArrayExpressionImpl(tree.metadata(),
      tree.elements().stream()
        .map(this::toExpression)
        .collect(Collectors.toList()));
  }

  public Expression toExpression(PropertyTree tree) {
    return toExpression((YamlTree) tree.value());
  }

  public Expression toExpression(YamlTree tree) {
    if (tree instanceof SequenceTree) {
      return toArrayExpression((SequenceTree) tree);
    } else if (tree instanceof MappingTree) {
      return toObjectExpression((MappingTree) tree);
    } else if (tree instanceof ScalarTree) {
      return toLiteralExpression((ScalarTree) tree);
    } else {
      throw new ParseException("Couldn't convert to Expression, unsupported class " + tree.getClass().getSimpleName(), new BasicTextPointer(tree.metadata().textRange()), null);
    }
  }

  public Expression toLiteralExpression(ScalarTree tree) {
    if (tree.style() == ScalarTree.Style.PLAIN) {
      if ("null".equals(tree.value())) {
        return new NullLiteralImpl(tree.metadata());
      } else if ("true".equals(tree.value()) || "false".equals(tree.value())) {
        return new BooleanLiteralImpl("true".equals(tree.value()), tree.metadata());
      } else {
        try {
          return new NumericLiteralImpl(Float.parseFloat(tree.value()), tree.metadata());
        } catch (NumberFormatException e) {
          throw new ParseException("Failed to parse plain value '" + tree.value() + "'", new BasicTextPointer(tree.metadata().textRange()), null);
        }
      }
    } else if (tree.style() == ScalarTree.Style.DOUBLE_QUOTED) {
      return new StringLiteralImpl(tree.value(), tree.metadata());
    } else {
      throw new ParseException("Unsupported ScalarTree style: " + tree.style().name(), new BasicTextPointer(tree.metadata().textRange()), null);
    }
  }

  protected List<Property> toProperties(Tree tree) {
    if (!(tree instanceof HasProperties)) {
      throw new ParseException("Couldn't convert properties: expecting object of class '" + tree.getClass().getSimpleName() + "' to implement HasProperties",
        new BasicTextPointer(tree.textRange()), null);
    }

    List<Property> properties = new ArrayList<>();
    for (PropertyTree propertyTree : ((HasProperties) tree).properties()) {
      Identifier key = toIdentifier((YamlTree) propertyTree.key());
      Expression value = toExpression((YamlTree) propertyTree.value());
      properties.add(new PropertyImpl(key, value));
    }
    return properties;
  }

  protected Predicate<TupleTree> filterOnField(String field) {
    return tupleTree -> tupleTree.key() instanceof ScalarTree && field.equals(((ScalarTree) tupleTree.key()).value());
  }

  public String filenameAndPosition(TextRange textRange) {
    String position = textRange.start().line() + ":" + textRange.start().lineOffset();
    if (inputFileContext != null) {
      String filename = inputFileContext.inputFile.toString();
      if (filename != null && !filename.isBlank()) {
        return filename + ":" + position;
      }
    }
    return position;
  }

  // Error generation
  protected ParseException missingMandatoryAttributeError(YamlTree tree, String key) {
    return new ParseException("Missing mandatory attribute '" + key + "' at " + filenameAndPosition(tree.metadata().textRange()),
      new BasicTextPointer(tree.metadata().textRange()), null);
  }

  private ParseException convertError(PropertyTree property, String targetType, String expectedType) {
    YamlTree value = (YamlTree) property.value();
    String errorMessage = convertErrorMessage(property.key(), targetType, property.value().textRange(), expectedType, value.getClass().getSimpleName());
    return new ParseException(errorMessage, new BasicTextPointer(value.textRange()), null);
  }

  private ParseException convertError(Tree tree, String targetType, String expectedType) {
    String errorMessage = convertErrorMessage(tree, targetType, tree.textRange(), expectedType, tree.getClass().getSimpleName());
    return new ParseException(errorMessage, new BasicTextPointer(tree.textRange()), null);
  }

  private ParseException convertError(PropertyTree property, ScalarTree value, String targetType, String expectedStyle) {
    String errorMessage = convertErrorMessage(property.key(), targetType, value.textRange(), expectedStyle, value.style().name());
    return new ParseException(errorMessage, new BasicTextPointer(value.textRange()), null);
  }

  private String convertErrorMessage(Tree objectToConvert, String targetType, TextRange location, String expectedValue, String valueFound) {
    String toConvert = TextUtils.getValue(objectToConvert).orElse(objectToConvert.toString());
    return "Couldn't convert '" + toConvert + "' into " + targetType + " at " + filenameAndPosition(location) + ": expecting " + expectedValue + ", got " + valueFound + " instead";
  }
}
