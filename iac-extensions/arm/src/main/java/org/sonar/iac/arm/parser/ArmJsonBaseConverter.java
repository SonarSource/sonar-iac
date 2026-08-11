/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.sonar.iac.arm.parser.bicep.ArmTemplateExpressionParser;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Parameter;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.Variable;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.impl.ParameterImpl;
import org.sonar.iac.arm.tree.impl.VariableImpl;
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
import org.sonar.iac.common.api.tree.HasTextRange;
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
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

import static org.sonar.iac.common.extension.ParseException.createParseException;

public class ArmJsonBaseConverter {
  private static final ArmTemplateExpressionParser ARM_TEMPLATE_EXPRESSION_PARSER = ArmTemplateExpressionParser.create();

  @Nullable
  protected final InputFileContext inputFileContext;

  public ArmJsonBaseConverter(@Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
  }

  @Nullable
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

  @Nullable
  public StringLiteral toNestedStringLiteralOrNull(@Nullable YamlTree tree, String parentKey, String childKey) {
    return PropertyUtils.get(tree, parentKey).map(m -> extractPropertyOrNull(m, childKey)).orElse(null);
  }

  public Identifier toIdentifier(YamlTree tree) {
    if (!(tree instanceof ScalarTree scalarTree)) {
      throw convertError(tree, Identifier.class.getSimpleName(), ScalarTree.class.getSimpleName());
    }
    return new IdentifierImpl(scalarTree.value(), scalarTree.metadata());
  }

  private ScalarTree toDoubleQuoteScalarTree(PropertyTree property) {
    if (!(property.value() instanceof ScalarTree value)) {
      throw convertError(property, StringLiteral.class.getSimpleName(), ScalarTree.class.getSimpleName());
    }
    if (value.style() != ScalarTree.Style.DOUBLE_QUOTED) {
      throw convertError(property, value, StringLiteral.class.getSimpleName(), "ScalarTree.Style.DOUBLE_QUOTED");
    }
    return value;
  }

  @Nullable
  public NumericLiteral toNumericLiteralOrNull(@Nullable YamlTree tree, String key) {
    return PropertyUtils.get(tree, key::equalsIgnoreCase)
      .map(this::toNumericLiteral)
      .orElse(null);
  }

  private NumericLiteral toNumericLiteral(PropertyTree property) {
    if (!(property.value() instanceof ScalarTree value)) {
      throw convertError(property, NumericLiteral.class.getSimpleName(), ScalarTree.class.getSimpleName());
    }
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

  @Nullable
  public ArrayExpression toArrayExpressionOrNull(@Nullable YamlTree tree, String key) {
    return PropertyUtils.get(tree, key::equalsIgnoreCase).map(this::toArrayExpression).orElse(null);
  }

  private ArrayExpression toArrayExpression(PropertyTree property) {
    if (!(property.value() instanceof SequenceTree sequenceTree)) {
      throw convertError(property, ArrayExpression.class.getSimpleName(), SequenceTree.class.getSimpleName());
    }
    return toArrayExpression(sequenceTree);
  }

  private ArrayExpression toArrayExpression(SequenceTree tree) {
    return new ArrayExpressionImpl(tree.metadata(),
      tree.elements().stream()
        .map(this::toExpression)
        .toList());
  }

  @Nullable
  public Expression toExpressionOrNull(TupleTree tree, String key) {
    return PropertyUtils.get(tree.value(), key::equalsIgnoreCase).map(this::toExpression).orElse(null);
  }

  public Expression toExpressionOrException(YamlTree tree, String key) {
    return PropertyUtils.get(tree, key::equalsIgnoreCase)
      .map(this::toExpression)
      .orElseThrow(() -> missingMandatoryAttributeError(tree, key));
  }

  /**
   * Every {@link PropertyTree} reaching the JSON converter originates from {@link MappingTree#elements()}, i.e. it is a
   * {@link org.sonar.iac.common.yaml.tree.TupleTree}, whose {@code value()} narrows {@link PropertyTree#value()} to a
   * non-null {@link YamlTree}. The cast is therefore safe.
   */
  private Expression toExpression(PropertyTree tree) {
    return toExpression((YamlTree) tree.value());
  }

  public Expression toExpression(@Nullable YamlTree tree) {
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
      var className = Optional.ofNullable(tree).map(Object::getClass).map(Class::getSimpleName).orElse("null");
      var textPointer = Optional.ofNullable(tree).map(YamlTree::metadata).map(YamlTreeMetadata::textRange).map(BasicTextPointer::new).orElse(null);
      throw createParseException("Couldn't convert to Expression, unsupported class " + className,
        inputFileContext,
        textPointer);
    }
  }

  private static boolean isArmJsonExpression(String value) {
    return value.startsWith("[") && value.endsWith("]") && value.charAt(1) != '[';
  }

  private Expression toExpressionFromString(ScalarTree scalar) {
    var expression = (Expression) ARM_TEMPLATE_EXPRESSION_PARSER.parse(scalar);

    // Repack top-level nodes so that their text ranges cover the entire text range of the expression
    if (expression instanceof FunctionCall functionCall) {
      return new FunctionCallImpl(scalar.metadata(), functionCall.name(), functionCall.argumentList());
    } else if (expression instanceof StringLiteral stringLiteral) {
      return new StringLiteralImpl(stringLiteral.value(), scalar.metadata());
    } else if (expression instanceof MemberExpression memberExpression) {
      return new MemberExpressionImpl(scalar.metadata(), memberExpression.expression(), memberExpression.separatingToken(), memberExpression.memberAccess());
    } else if (expression instanceof Parameter parameter) {
      return new ParameterImpl(parameter.identifier(), scalar.textRange());
    } else if (expression instanceof Variable variable) {
      return new VariableImpl(variable.identifier(), scalar.textRange());
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
    if (tree instanceof ScalarTree) {
      return Collections.emptyList();
    }
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

  @Nullable
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
    var valueTypeName = Optional.ofNullable(property.value()).map(Object::getClass).map(Class::getSimpleName).orElse("null");
    var textPointer = Optional.ofNullable(property.value()).map(HasTextRange::textRange).map(BasicTextPointer::new).orElse(null);
    String errorMessage = convertErrorMessage(property.key(), targetType, expectedType, valueTypeName);
    return createParseException(errorMessage, inputFileContext, textPointer);
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
