/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
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
    return PropertyUtils.value(logGroupResource.properties(), "LogGroupName", CloudformationTree.class)
      .map(LogGroupDeclarationCheck::resolveIdentifiersFromProperty)
      .orElse(Collections.emptySet());
  }

  // Reference identifiers can be resolved as function names directly from scalar properties or extracted from intrinsic functions
  private static Set<String> resolveIdentifiersFromProperty(CloudformationTree property) {
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
      register(ScalarTree.class, (ctx, tree) -> {
        if ("!Sub".equals(tree.tag())) {
          collectSubParameters(tree);
        } else if ("!Ref".equals(tree.tag())) {
          collectRefParameter(tree);
        }
      });
      register(TupleTree.class, (ctx, tree) -> {
        if (TextUtils.isValue(tree.key(), "Fn::Sub").isTrue()) {
          collectSubParameters(tree.value());
        } else if(TextUtils.isValue(tree.key(), "Ref").isTrue()) {
          collectRefParameter(tree.value());
        }
      });
    }

    private void collectSubParameters(CloudformationTree sub) {
      if (sub instanceof ScalarTree) {
        Matcher m = SUB_PARAMETERS.matcher(((ScalarTree) sub).value());
        while (m.find()) {
          if (m.group(1) != null) {
            references.add(m.group(1));
          } else {
            references.add(m.group(2));
          }
        }
      }
    }

    private void collectRefParameter(CloudformationTree ref) {
      if (ref instanceof ScalarTree) {
        references.add(((ScalarTree) ref).value());
      }
    }

    public static Set<String> get(CloudformationTree logGroupNameProperty) {
      FunctionReferenceCollector collector = new FunctionReferenceCollector();
      collector.scan(new TreeContext(), logGroupNameProperty);
      return collector.references;
    }
  }
}
