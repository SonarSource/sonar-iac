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
package org.sonar.iac.arm.checks;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6954")
public class EmptyOrNullValueCheck implements IacCheck {
  private static final String MESSAGE = "Remove this %s or complete with real code.";
  private static final String DEPLOYMENT_RESOURCE_TYPE = "Microsoft.Resources/deployments";
  private static final String PROPERTIES = "properties";
  private static final Map<ArmTree.Kind, String> TYPE_TO_STRING = Map.of(
    ArmTree.Kind.NULL_LITERAL, "null %s",
    ArmTree.Kind.STRING_LITERAL, "empty string",
    ArmTree.Kind.OBJECT_EXPRESSION, "empty object",
    ArmTree.Kind.ARRAY_EXPRESSION, "empty array");
  private static final String DEFAULT_IGNORED_PROPERTIES = "";
  private Set<String> ignoredPropertiesSet = Set.of();

  /**
   * Map of resource types to properties that should be ignored when empty.
   * These are properties that are commonly empty by design (e.g., when using RBAC instead of access policies).
   * Resource type comparison is case-insensitive and version-agnostic.
   */
  private static final Map<String, Set<String>> IGNORED_EMPTY_PROPERTIES_BY_RESOURCE_TYPE = Map.of(
    "microsoft.keyvault/vaults", Set.of("accessPolicies"),
    "microsoft.network/networksecuritygroups", Set.of("securityRules"));

  @RuleProperty(
    key = "ignoredProperties",
    description = "Comma separated list of ignored properties.")
  public String ignoredProperties = DEFAULT_IGNORED_PROPERTIES;

  private static Set<String> initializeIgnoredProperties(String properties) {
    return Arrays.stream(properties.split(","))
      .map(String::trim)
      .filter(s -> !s.isBlank())
      .collect(Collectors.toSet());
  }

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, this::checkResource);
    init.register(VariableDeclaration.class, this::checkVariable);
    init.register(OutputDeclaration.class, this::checkOutput);
    ignoredPropertiesSet = initializeIgnoredProperties(ignoredProperties);
  }

  private void checkResource(CheckContext ctx, ResourceDeclaration resource) {
    for (Property property : resource.resourceProperties()) {
      var propertyName = property.key().value();
      if (!isPropertyException(property, resource) && !ignoredPropertiesSet.contains(propertyName)) {
        checkExpression(ctx, property, property.value(), "property", resource);
      }
    }
  }

  private void checkVariable(CheckContext ctx, VariableDeclaration variable) {
    var varName = variable.declaratedName().value();
    if (!ignoredPropertiesSet.contains(varName)) {
      checkExpression(ctx, variable, variable.value(), "variable", null);
    }
  }

  private void checkOutput(CheckContext ctx, OutputDeclaration output) {
    var outputValue = output.value();
    var outputName = output.declaratedName().value();
    if (outputValue == null || ignoredPropertiesSet.contains(outputName)) {
      return;
    }

    if (outputValue.is(ArmTree.Kind.FOR_EXPRESSION)) {
      checkForOutput(ctx, (ForExpression) outputValue);
    } else {
      checkExpression(ctx, output, outputValue, "output", null);
    }
  }

  /**
   * Exceptions for resource properties:
   * <ul>
   *   <li>Top-level `properties` of a resource declaration</li>
   *   <li>Property `userAssignedIdentities -> {ID}`, where ID value has to be null for several resource types, and ID
   *   itself can be an arbitrary string or variable</li>
   *   <li>Properties defined in {@link #IGNORED_EMPTY_PROPERTIES_BY_RESOURCE_TYPE} for specific resource types</li>
   * </ul>
   */
  private static boolean isPropertyException(Property property, @Nullable ResourceDeclaration resource) {
    return isTopLevelPropertiesProperty(property)
      || isUserAssignedIdentitiesIdProperty(property)
      || isMatchingNestedTemplateParameterDefault(property, resource)
      || isIgnoredEmptyPropertyForResourceType(property, resource);
  }

  private static boolean isTopLevelPropertiesProperty(Property property) {
    return TextUtils.isValue(property.key(), PROPERTIES).isTrue() && isEmpty(property.value());
  }

  private static boolean isUserAssignedIdentitiesIdProperty(Property property) {
    return Optional.ofNullable(property.parent())
      .map(ArmTree::parent)
      .map(parent -> parent instanceof Property parentProperty && TextUtils.isValue(parentProperty.key(), "userAssignedIdentities").isTrue())
      .orElse(false);
  }

  /**
   * Checks whether an empty default matches its nested template parameter type.
   *
   * @param property the default value property
   * @param resource the enclosing resource
   * @return {@code true} when the empty default is valid for the parameter type
   */
  private static boolean isMatchingNestedTemplateParameterDefault(Property property, @Nullable ResourceDeclaration resource) {
    @Nullable
    Expression defaultValue = property.value();
    if (resource == null
      || defaultValue == null
      || !resource.type().value().equalsIgnoreCase(DEPLOYMENT_RESOURCE_TYPE)
      || !TextUtils.isValue(property.key(), "defaultValue").isTrue()
      || (!isEmptyObject(defaultValue) && !isEmptyArray(defaultValue))) {
      return false;
    }

    ArmTree parameterObject = property.parent();
    if (!(parameterObject instanceof ObjectExpression parameter)
      || !(parameter.parent() instanceof Property parameterProperty)) {
      return false;
    }

    ArmTree parametersObject = parameterProperty.parent();
    if (!(parametersObject instanceof ObjectExpression parameters)
      || !(parameters.parent() instanceof Property parametersProperty)
      || !TextUtils.isValue(parametersProperty.key(), "parameters").isTrue()) {
      return false;
    }

    ArmTree templateObject = parametersProperty.parent();
    return templateObject instanceof ObjectExpression template
      && template.parent() instanceof Property templateProperty
      && TextUtils.isValue(templateProperty.key(), "template").isTrue()
      && isDeploymentTemplate(templateProperty, resource)
      && hasMatchingParameterType(parameter, defaultValue);
  }

  /**
   * Checks whether a template belongs to the deployment resource or to a deployment nested in that template.
   */
  private static boolean isDeploymentTemplate(Property templateProperty, ResourceDeclaration resource) {
    if (!TextUtils.isValue(templateProperty.key(), "template").isTrue()
      || !(templateProperty.parent() instanceof ObjectExpression properties)) {
      return false;
    }
    if (resource.getResourceProperty(PROPERTIES).filter(property -> property.value() == properties).isPresent()) {
      return true;
    }
    if (!(properties.parent() instanceof Property propertiesProperty)
      || !TextUtils.isValue(propertiesProperty.key(), PROPERTIES).isTrue()
      || !(propertiesProperty.parent() instanceof ObjectExpression nestedResource)
      || nestedResource.properties().stream().noneMatch(property -> TextUtils.isValue(property.key(), "type").isTrue()
        && TextUtils.isValue(property.value(), DEPLOYMENT_RESOURCE_TYPE).isTrue())) {
      return false;
    }

    ArmTree container = nestedResource.parent();
    if (container instanceof Property symbolicResource && symbolicResource.parent() instanceof ObjectExpression resources) {
      container = resources;
    }
    return (container instanceof ArrayExpression || container instanceof ObjectExpression)
      && container.parent() instanceof Property resourcesProperty
      && TextUtils.isValue(resourcesProperty.key(), "resources").isTrue()
      && resourcesProperty.parent() instanceof ObjectExpression parentTemplate
      && parentTemplate.parent() instanceof Property parentTemplateProperty
      && isDeploymentTemplate(parentTemplateProperty, resource);
  }

  /**
   * Checks whether a parameter declaration type matches an empty default value.
   *
   * @param parameter the parameter declaration object
   * @param defaultValue the default value to validate
   * @return {@code true} when the declared type matches the empty value
   */
  private static boolean hasMatchingParameterType(ObjectExpression parameter, Expression defaultValue) {
    return parameter.properties().stream()
      .map(Property.class::cast)
      .filter(property -> TextUtils.isValue(property.key(), "type").isTrue())
      .map(Property::value)
      .filter(StringLiteral.class::isInstance)
      .map(StringLiteral.class::cast)
      .map(StringLiteral::value)
      .anyMatch(type -> (isEmptyObject(defaultValue) && "object".equalsIgnoreCase(type))
        || (isEmptyArray(defaultValue) && "array".equalsIgnoreCase(type)));
  }

  /**
   * Checks if the property is in the list of ignored empty properties for the given resource type.
   */
  private static boolean isIgnoredEmptyPropertyForResourceType(Property property, @Nullable ResourceDeclaration resource) {
    if (resource == null || !isEmpty(property.value())) {
      return false;
    }

    String resourceType = resource.type().value().toLowerCase(Locale.ROOT);
    Set<String> ignoredProperties = IGNORED_EMPTY_PROPERTIES_BY_RESOURCE_TYPE.get(resourceType);

    if (ignoredProperties == null) {
      return false;
    }

    String propertyName = property.key().value();
    return ignoredProperties.contains(propertyName);
  }

  private void checkExpression(CheckContext ctx, HasTextRange propertyToReport, Expression expression, String kind, @Nullable ResourceDeclaration resource) {
    if (isEmpty(expression)) {
      ctx.reportIssue(propertyToReport, message(expression.getKind(), kind));
    } else if (expression.is(ArmTree.Kind.OBJECT_EXPRESSION)) {
      checkObject(ctx, (ObjectExpression) expression, resource);
    } else if (expression.is(ArmTree.Kind.ARRAY_EXPRESSION)) {
      checkArray(ctx, (ArrayExpression) expression, resource);
    }
  }

  private void checkObject(CheckContext ctx, ObjectExpression object, @Nullable ResourceDeclaration resource) {
    for (PropertyTree property : object.properties()) {
      var value = property.value();
      var name = TextUtils.getValue(property.key()).orElse("");
      if (value instanceof Expression expression
        && !isPropertyException((Property) property, resource)
        && !ignoredPropertiesSet.contains(name)) {
        checkExpression(ctx, property, expression, "property", resource);
      }
    }
  }

  private void checkArray(CheckContext ctx, ArrayExpression array, @Nullable ResourceDeclaration resource) {
    for (Expression expression : array.elements()) {
      if (expression instanceof ObjectExpression object) {
        checkObject(ctx, object, resource);
      }
    }
  }

  private void checkForOutput(CheckContext ctx, ForExpression forExpression) {
    if (forExpression.bodyExpression().is(ArmTree.Kind.OBJECT_EXPRESSION)) {
      ObjectExpression forBody = (ObjectExpression) forExpression.bodyExpression();
      for (PropertyTree property : forBody.properties()) {
        Tree value = property.value();
        if (value instanceof Expression expression) {
          checkExpression(ctx, property, expression, "output", null);
        }
      }
    }
  }

  private static String message(ArmTree.Kind kind, String type) {
    if (kind == ArmTree.Kind.NULL_LITERAL) {
      return MESSAGE.formatted(TYPE_TO_STRING.get(kind).formatted(type));
    } else {
      return MESSAGE.formatted(TYPE_TO_STRING.get(kind));
    }
  }

  private static boolean isEmpty(@Nullable Expression expression) {
    return expression != null && (expression.is(ArmTree.Kind.NULL_LITERAL) || isEmptyString(expression) || isEmptyObject(expression) || isEmptyArray(expression));
  }

  private static boolean isEmptyString(Expression expression) {
    return expression.is(ArmTree.Kind.STRING_LITERAL) && ((StringLiteral) expression).value().isEmpty();
  }

  private static boolean isEmptyObject(Expression expression) {
    return expression.is(ArmTree.Kind.OBJECT_EXPRESSION) && ((ObjectExpression) expression).properties().isEmpty();
  }

  private static boolean isEmptyArray(Expression expression) {
    return expression.is(ArmTree.Kind.ARRAY_EXPRESSION) && ((ArrayExpression) expression).elements().isEmpty();
  }
}
