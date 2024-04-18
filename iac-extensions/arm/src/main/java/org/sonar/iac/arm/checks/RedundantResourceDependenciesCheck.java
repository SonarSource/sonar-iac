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
package org.sonar.iac.arm.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isFunctionCall;

@Rule(key = "S6952")
public class RedundantResourceDependenciesCheck implements IacCheck {
  private static final String MESSAGE = "Remove this explicit dependency as it is already defined implicitly.";
  private static final String SECONDARY_MESSAGE_REFERENCE = "Implicit dependency is created via the \"reference\" function.";
  private static final String SECONDARY_MESSAGE_SYMBOLIC = "Implicit dependency is created via a symbolic name.";
  // Implicit dependency can be expressed only with `reference(s)?` functions:
  // https://learn.microsoft.com/en-us/azure/azure-resource-manager/templates/template-functions-resource#implicit-dependency
  // https://learn.microsoft.com/en-us/azure/azure-resource-manager/templates/template-functions-resource#implicit-dependency-1
  private static final Predicate<Expression> IS_REFERENCE_FUNCTION = isFunctionCall("reference").or(isFunctionCall("references"));

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, RedundantResourceDependenciesCheck::collectExplicitDependencies);
  }

  private static void collectExplicitDependencies(CheckContext checkContext, ResourceDeclaration resourceDeclaration) {
    var resourceExplicitDependencies = resourceDeclaration.getResourceProperty("dependsOn")
      .map(Property::value)
      .filter(ArrayExpression.class::isInstance)
      .map(ArrayExpression.class::cast);
    if (resourceExplicitDependencies.isEmpty()) {
      return;
    }

    var explicitDependencies = collectDependencies(resourceExplicitDependencies.get());
    var referencedResources = collectReferences(resourceDeclaration);
    var referencedResourcesSymbolic = collectSymbolicReferences(resourceDeclaration);

    for (var dependency : explicitDependencies) {
      String dependencyName = dependency.value();

      var references = new ArrayList<TextTree>();
      for (var referencedResource : referencedResources) {
        String referencedName = referencedResource.value();
        if (dependencyName.equals(referencedName)) {
          references.add(referencedResource);
        }
      }

      var symbolicReferences = new ArrayList<TextTree>();
      for (var referencedSymbol : referencedResourcesSymbolic) {
        var referencedName = referencedSymbol.value();
        if (dependencyName.equals(referencedName)) {
          symbolicReferences.add(referencedSymbol);
        }
      }

      var secondaryLocations = Stream.concat(
        references.stream().map(textTree -> new SecondaryLocation(textTree, SECONDARY_MESSAGE_REFERENCE)),
        symbolicReferences.stream().map(textTree -> new SecondaryLocation(textTree, SECONDARY_MESSAGE_SYMBOLIC))).toList();
      if (!secondaryLocations.isEmpty()) {
        checkContext.reportIssue(dependency, MESSAGE, secondaryLocations);
      }
    }
  }

  private static List<TextTree> collectDependencies(ArrayExpression dependsOn) {
    // The value can be a comma-separated list of a resource names or resource unique identifiers:
    // https://learn.microsoft.com/en-us/azure/azure-resource-manager/templates/syntax#resources
    return dependsOn.elements().stream().map(RedundantResourceDependenciesCheck::extractDependencyTextTree)
      .filter(Objects::nonNull)
      .toList();
  }

  /**
   * Resource names are either `StringLiteral`s in JSON or Identifiers in Bicep
   * Resource IDs are returned by `*ResourceId` functions
   * There can be other types of expressions, that eventually resolve into strings, but we can't evaluate them; these can be FNs.
   */
  @CheckForNull
  private static TextTree extractDependencyTextTree(Expression expression) {
    if (expression instanceof FunctionCall functionCall &&
      TextUtils.matchesValue(functionCall.name(), name -> "resourceId".equals(name) || name.endsWith("ResourceId")).isTrue()) {
      if (functionCall.argumentList().elements().size() == 2 && functionCall.argumentList().elements().get(1) instanceof TextTree textTree) {
        // TODO SONARIAC-1426: S6952: Cover `*ResourceId` functions in the `dependsOn` block
        // There are multiple overloads of this function. For now, we cover only the case where two mandatory arguments are provided:
        // the resource type the resource name.
        return textTree;
      }
    } else if (expression instanceof MemberExpression memberExpression) {
      return extractDependencyTextTree(memberExpression.memberAccess());
    } else if (expression instanceof HasIdentifier hasIdentifier && hasIdentifier.identifier() instanceof TextTree textTree) {
      return textTree;
    } else if (expression instanceof StringLiteral stringLiteral) {
      return stringLiteral;
    }
    return null;
  }

  private static List<TextTree> collectReferences(ResourceDeclaration resourceDeclaration) {
    // Traverse all nodes of all property values and find expressions that can be references to other resources. This includes:
    // * function calls to `reference[s]?` function
    // * `Identifier`s, because they can be usages of symbolic names

    return resourceDeclaration.properties().stream().flatMap(property -> getAllPropertyValues(property.value()))
      .flatMap(expression -> ArmTreeUtils.findAllNodes(expression, FunctionCall.class))
      .filter(IS_REFERENCE_FUNCTION)
      .map(functionCall -> functionCall.argumentList().elements().get(0))
      .filter(StringLiteral.class::isInstance)
      .map(TextTree.class::cast)
      .toList();
  }

  private static List<TextTree> collectSymbolicReferences(ResourceDeclaration resourceDeclaration) {
    return resourceDeclaration.properties().stream().flatMap(property -> getAllPropertyValues(property.value()))
      .flatMap(expression -> ArmTreeUtils.findAllNodes(expression, HasIdentifier.class))
      .map(HasIdentifier::identifier)
      .filter(TextTree.class::isInstance)
      .map(TextTree.class::cast)
      .toList();
  }

  private static Stream<Expression> getAllPropertyValues(Expression expression) {
    if (expression instanceof ObjectExpression objectExpression) {
      return objectExpression.allPropertiesFlattened().flatMap(prop -> getAllPropertyValues(prop.value()));
    } else if (expression instanceof ArrayExpression arrayExpression) {
      return arrayExpression.elements().stream().flatMap(RedundantResourceDependenciesCheck::getAllPropertyValues);
    } else {
      return Stream.of(expression);
    }
  }
}
