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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.parser.bicep.ArmTemplateExpressionParser;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.impl.json.ArrayExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.BooleanLiteralImpl;
import org.sonar.iac.arm.tree.impl.json.FunctionCallImpl;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.json.MemberExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.NullLiteralImpl;
import org.sonar.iac.arm.tree.impl.json.NumericLiteralImpl;
import org.sonar.iac.arm.tree.impl.json.ObjectExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.PropertyImpl;
import org.sonar.iac.arm.tree.impl.json.StringLiteralImpl;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.sonar.iac.common.extension.ParseException.createParseException;

public class ArmJsonBaseConverter {
  private static final ArmTemplateExpressionParser ARM_TEMPLATE_EXPRESSION_PARSER = ArmTemplateExpressionParser.create();

  @Nullable
  protected final InputFileContext inputFileContext;

  public ArmJsonBaseConverter(@Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
  }

  public StringLiteral toStringLiteralOrNull(YamlTree tree, String key) {
    return PropertyUtils.get(tree, key::equalsIgnoreCase)
      .map(this::toStringLiteral)
      .orElse(null);
  }

  public StringLiteral toStringLiteralOrException(YamlTree tree, String key) {
    return PropertyUtils.get(tree, key::equalsIgnoreCase)
      .map(this::toStringLiteral)
      .orElseThrow(() -> missingMandatoryAttributeError(tree, key));
  }

  private StringLiteral toStringLiteral(PropertyTree property) {
    ScalarTree value = toDoubleQuoteScalarTree(property);
    return new StringLiteralImpl(value.value(), value.metadata());
  }

  public StringLiteral toNestedStringLiteralOrNull(YamlTree tree, String parentKey, String childKey) {
    return PropertyUtils.get(tree, parentKey).map(m -> extractPropertyOrNull(m, childKey)).orElse(null);
  }

  public Identifier toIdentifier(YamlTree tree) {
    if (!(tree instanceof ScalarTree)) {
      throw convertError(tree, Identifier.class.getSimpleName(), ScalarTree.class.getSimpleName());
    }
    ScalarTree scalarTree = (ScalarTree) tree;
    return new IdentifierImpl(scalarTree.value(), scalarTree.metadata());
  }

  private ScalarTree toDoubleQuoteScalarTree(PropertyTree property) {
    if (!(property.value() instanceof ScalarTree)) {
      throw convertError(property, StringLiteral.class.getSimpleName(), ScalarTree.class.getSimpleName());
    }
    ScalarTree value = (ScalarTree) property.value();
    if (value.style() != ScalarTree.Style.DOUBLE_QUOTED) {
      throw convertError(property, value, StringLiteral.class.getSimpleName(), "ScalarTree.Style.DOUBLE_QUOTED");
    }
    return value;
  }

  public NumericLiteral toNumericLiteralOrNull(YamlTree tree, String key) {
    return PropertyUtils.get(tree, key::equalsIgnoreCase)
      .map(this::toNumericLiteral)
      .orElse(null);
  }

  private NumericLiteral toNumericLiteral(PropertyTree property) {
    if (!(property.value() instanceof ScalarTree)) {
      throw convertError(property, NumericLiteral.class.getSimpleName(), ScalarTree.class.getSimpleName());
    }
    ScalarTree value = (ScalarTree) property.value();
    if (value.style() != ScalarTree.Style.PLAIN) {
      throw convertError(property, value, NumericLiteral.class.getSimpleName(), "ScalarTree.Style.PLAIN");
    }
    try {
      // for validation
      Double.parseDouble(value.value());

      return new NumericLiteralImpl(value.value(), value.metadata());
    } catch (NumberFormatException e) {
      throw createParseException(
        "Failed to parse float value '" + value.value(),
        inputFileContext,
        new BasicTextPointer(value.textRange()));
    }
  }

  private ObjectExpression toObjectExpression(MappingTree tree) {
    List<Property> properties = new ArrayList<>();
    tree.elements()
      .forEach(tupleTree -> {
        Identifier key = toIdentifier(tupleTree.key());
        Expression value = toExpression(tupleTree.value());
        properties.add(new PropertyImpl(key, value));
      });
    // Objects can be empty so the text range calculation based on children can not be applied
    return new ObjectExpressionImpl(properties, tree.textRange());
  }

  public ArrayExpression toArrayExpressionOrNull(YamlTree tree, String key) {
    return PropertyUtils.get(tree, key::equalsIgnoreCase).map(this::toArrayExpression).orElse(null);
  }

  private ArrayExpression toArrayExpression(PropertyTree property) {
    if (!(property.value() instanceof SequenceTree)) {
      throw convertError(property, ArrayExpression.class.getSimpleName(), SequenceTree.class.getSimpleName());
    }
    return toArrayExpression((SequenceTree) property.value());
  }

  private ArrayExpression toArrayExpression(SequenceTree tree) {
    return new ArrayExpressionImpl(tree.metadata(),
      tree.elements().stream()
        .map(this::toExpression)
        .toList());
  }

  public Expression toExpressionOrNull(TupleTree tree, String key) {
    return PropertyUtils.get(tree.value(), key::equalsIgnoreCase).map(this::toExpression).orElse(null);
  }

  private Expression toExpression(PropertyTree tree) {
    return toExpression((YamlTree) tree.value());
  }

  public Expression toExpression(YamlTree tree) {
    if (tree instanceof SequenceTree sequence) {
      return toArrayExpression(sequence);
    } else if (tree instanceof MappingTree mapping) {
      return toObjectExpression(mapping);
    } else if (tree instanceof ScalarTree scalar) {
      if (isArmJsonExpression(scalar.value())) {
        return toExpressionFromString(scalar);
      }
      return toLiteralExpression(scalar);
    } else {
      throw createParseException("Couldn't convert to Expression, unsupported class " + tree.getClass().getSimpleName(),
        inputFileContext,
        new BasicTextPointer(tree.metadata().textRange()));
    }
  }

  private static boolean isArmJsonExpression(String value) {
    return value.startsWith("[") && value.endsWith("]") && value.charAt(1) != '[';
  }

  private Expression toExpressionFromString(ScalarTree scalar) {
    // Replacing line breaks, because the original string in JSON is definitely one-liner, but can contain line breaks which we should
    // treat as escaped symbols.
    var expression = (Expression) ARM_TEMPLATE_EXPRESSION_PARSER.parse(scalar);

    // TODO SONARIAC-1405: ARM template expressions: replace `variables()` and `parameters()` with corresponding Identifiers
    if (expression instanceof FunctionCall functionCall) {
      return new FunctionCallImpl(scalar.metadata(), functionCall.name(), functionCall.argumentList());
    } else if (expression instanceof StringLiteral stringLiteral) {
      return new StringLiteralImpl(stringLiteral.value(), scalar.metadata());
    } else if (expression instanceof MemberExpression memberExpression) {
      return new MemberExpressionImpl(scalar.metadata(), memberExpression.expression(), memberExpression.separatingToken(), memberExpression.memberAccess());
    } else {
      throw createParseException("Failed to parse ARM template expression: " + scalar.value() + "; top-level expression is of kind " + expression.getKind(),
        inputFileContext, new BasicTextPointer(scalar.metadata().textRange()));
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
          // for validation
          Double.parseDouble(tree.value());

          return new NumericLiteralImpl(tree.value(), tree.metadata());
        } catch (NumberFormatException e) {
          throw createParseException(
            "Failed to parse plain value '" + tree.value() + "'",
            inputFileContext,
            new BasicTextPointer(tree.metadata().textRange()));
        }
      }
    } else if (tree.style() == ScalarTree.Style.DOUBLE_QUOTED) {
      return new StringLiteralImpl(tree.value(), tree.metadata());
    } else {
      throw createParseException(
        "Unsupported ScalarTree style: " + tree.style().name(),
        inputFileContext,
        new BasicTextPointer(tree.metadata().textRange()));
    }
  }

  protected List<Property> toProperties(Tree tree) {
    if (!(tree instanceof HasProperties)) {
      throw createParseException(
        "Couldn't convert properties: expecting object of class '" + tree.getClass().getSimpleName() + "' to implement HasProperties",
        inputFileContext,
        new BasicTextPointer(tree.textRange()));
    }

    List<Property> properties = new ArrayList<>();
    for (PropertyTree propertyTree : ((HasProperties) tree).properties()) {
      var key = toIdentifier((YamlTree) propertyTree.key());
      var value = toExpression((YamlTree) propertyTree.value());
      properties.add(new PropertyImpl(key, value));
    }
    return properties;
  }

  @CheckForNull
  private StringLiteral extractPropertyOrNull(PropertyTree property, String name) {
    return PropertyUtils.get(property.value(), name::equalsIgnoreCase)
      .map(this::toStringLiteral)
      .orElse(null);
  }

  protected Stream<TupleTree> extractMappingToTupleTreeOnField(MappingTree document, String fieldName) {
    return document.elements().stream()
      .filter(filterOnField(fieldName))
      .map(TupleTree::value)
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .map(MappingTree::elements)
      .flatMap(List::stream);
  }

  protected Predicate<TupleTree> filterOnField(String field) {
    return tupleTree -> tupleTree.key() instanceof ScalarTree scalar && field.equalsIgnoreCase(scalar.value());
  }

  // Error generation
  protected ParseException missingMandatoryAttributeError(YamlTree tree, String key) {
    return createParseException(
      "Missing mandatory attribute '" + key + "'",
      inputFileContext,
      new BasicTextPointer(tree.metadata().textRange()));
  }

  private ParseException convertError(PropertyTree property, String targetType, String expectedType) {
    YamlTree value = (YamlTree) property.value();
    String errorMessage = convertErrorMessage(property.key(), targetType, expectedType, value.getClass().getSimpleName());
    return createParseException(errorMessage, inputFileContext, new BasicTextPointer(value.textRange()));
  }

  private ParseException convertError(Tree tree, String targetType, String expectedType) {
    String errorMessage = convertErrorMessage(tree, targetType, expectedType, tree.getClass().getSimpleName());
    return createParseException(errorMessage, inputFileContext, new BasicTextPointer(tree.textRange()));
  }

  private ParseException convertError(PropertyTree property, ScalarTree value, String targetType, String expectedStyle) {
    String errorMessage = convertErrorMessage(property.key(), targetType, expectedStyle, value.style().name());
    return createParseException(errorMessage, inputFileContext, new BasicTextPointer(value.textRange()));
  }

  private static String convertErrorMessage(Tree objectToConvert, String targetType, String expectedValue, String valueFound) {
    String toConvert = TextUtils.getValue(objectToConvert).orElse(objectToConvert.toString());
    return "Couldn't convert '" + toConvert + "' into " + targetType + ": expecting " + expectedValue + ", got " + valueFound + " instead";
  }
}
