/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.cloudformation.checks;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.cloudformation.tree.FunctionCallTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.cloudformation.checks.AbstractResourceCheck.Resource;
import org.sonar.iac.cloudformation.checks.utils.XPathUtils;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

import static org.sonar.iac.cloudformation.checks.AbstractResourceCheck.getFileResources;

@Rule(key = "S6294")
public class LogGroupDeclarationCheck implements IacCheck {

  private static final String MESSAGE = "Make sure missing \"Log Groups\" declaration is intended here.";
  private static final Set<String> RELEVANT_RESOURCE = new HashSet<>(Arrays.asList(
    "AWS::Lambda::Function",
    "AWS::Serverless::Function",
    "AWS::ApiGatewayV2::Api",
    "AWS::CodeBuild::Project"
  ));

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> {
      List<Resource> resources = getFileResources(tree);

      // Collect reference identifiers from LogGroup resources
      Set<String> referencedResourceIdentifier = resources.stream()
        .filter(resource -> resource.isType("AWS::Logs::LogGroup"))
        .filter(resource -> resource.properties() instanceof MappingTree)
        .map(LogGroupDeclarationCheck::getReferenceIdentifiers)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

      // Filter affected resources by LogGroup identifiers and raise issues on remaining resources without declared LogGroup
      resources.stream()
        .filter(LogGroupDeclarationCheck::isRelevantResource)
        .filter(r -> !matchResourceIdentifier(referencedResourceIdentifier, r))
        .forEach(resource -> ctx.reportIssue(resource.type(), MESSAGE));
    });
  }

  // Return extracted reference identifiers for a certain LogGroup resource
  private static Set<String> getReferenceIdentifiers(Resource logGroupResource) {
    return PropertyUtils.value(logGroupResource.properties(), "LogGroupName", YamlTree.class)
      .map(LogGroupDeclarationCheck::resolveIdentifiersFromProperty)
      .orElse(Collections.emptySet());
  }

  // Reference identifiers can be resolved as function names directly from scalar properties or extracted from intrinsic functions
  private static Set<String> resolveIdentifiersFromProperty(YamlTree property) {
    // We have to check the tag type and not the property value instance due to some functions are also represented as ScalarTrees
    if (property.tag().endsWith("str")) {
      // extract function name from LogGroupName (e.g /aws/lambda/my-function-name -> my-function-name)
      String value = ((ScalarTree) property).value();
      return Collections.singleton(value.substring(value.lastIndexOf("/") + 1));
    }
    return FunctionReferenceCollector.get(property);
  }

  private static boolean matchResourceIdentifier(Set<String> identifiers, Resource resource) {
    return identifiers.contains(resource.name().value()) || identifiers.contains(functionName(resource).orElse(null));
  }

  private static Optional<String> functionName(Resource resource) {
    return PropertyUtils.value(resource.properties(), "FunctionName")
      .filter(ScalarTree.class::isInstance).map(s -> ((ScalarTree) s).value());
  }

  private static boolean isRelevantResource(Resource resource) {
    return RELEVANT_RESOURCE.contains(TextUtils
      .getValue(resource.type())
      .orElse(null)) && !hasLogEvent(resource);
  }

  private static boolean hasLogEvent(Resource resource) {
    return PropertyUtils.value(resource.properties(), "Events")
      .filter(MappingTree.class::isInstance).map(e -> ((MappingTree) e).elements())
      .orElse(Collections.emptyList()).stream()
      .map(TupleTree::value)
      .anyMatch(e -> XPathUtils.getSingleTree(e, "/Properties/LogGroupName").isPresent());
  }

  // Instinct functions can be nested in the LogGroupName property value and can be extracted by a collecting TreeVisitor
  static class FunctionReferenceCollector extends TreeVisitor<TreeContext> {
    private static final Pattern SUB_PARAMETERS = Pattern.compile("\\$\\{([a-zA-Z0-9.]+)}|([a-zA-Z0-9.]+)");
    private final Set<String> references = new HashSet<>();

    public FunctionReferenceCollector() {
      register(FunctionCallTree.class, (ctx, tree) ->
        tree.arguments().stream().limit(1).filter(ScalarTree.class::isInstance)
          .forEach(argument -> collectReference(tree.name(), (ScalarTree) argument)));
    }

    private void collectReference(String functionName, ScalarTree argument) {
      if ("Sub".equals(functionName)) {
        collectSubParameters(argument);
      } else if ("Ref".equals(functionName)) {
        collectRefParameter(argument);
      }
    }

    private void collectSubParameters(ScalarTree subArgument) {
      Matcher m = SUB_PARAMETERS.matcher(subArgument.value());
      while (m.find()) {
        if (m.group(1) != null) {
          references.add(m.group(1));
        } else {
          references.add(m.group(2));
        }
      }
    }

    private void collectRefParameter(ScalarTree ref) {
      references.add(ref.value());
    }

    public static Set<String> get(YamlTree logGroupNameProperty) {
      FunctionReferenceCollector collector = new FunctionReferenceCollector();
      collector.scan(new TreeContext(), logGroupNameProperty);
      return collector.references;
    }
  }
}
