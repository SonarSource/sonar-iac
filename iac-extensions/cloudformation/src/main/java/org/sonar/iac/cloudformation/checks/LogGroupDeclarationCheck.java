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
import org.sonar.api.internal.apachecommons.lang.StringUtils;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.cloudformation.checks.AbstractResourceCheck.Resource;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.cloudformation.checks.utils.ScalarTreeUtils;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
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
    return MappingTreeUtils.getValue(logGroupResource.properties(), "LogGroupName")
      .map(LogGroupDeclarationCheck::resolveIdentifiersFromProperty)
      .orElse(Collections.emptySet());
  }

  // Reference identifiers can be resolved as function names directly from scalar properties or extracted from intrinsic functions
  private static Set<String> resolveIdentifiersFromProperty(CloudformationTree property) {
    // We have to check the tag type and not the property value instance due to some functions are also represented as ScalarTrees
    if (property.tag().endsWith("str")) {
      // extract function name from LogGroupName (e.g /aws/lambda/my-function-name -> my-function-name)
      return Collections.singleton(StringUtils.substringAfterLast(((ScalarTree) property).value(), "/"));
    }
    return FunctionReferenceCollector.get(property);
  }

  private static boolean matchResourceIdentifier(Set<String> identifiers, Resource resource) {
    return identifiers.contains(resource.name().value()) || identifiers.contains(functionName(resource).orElse(null));
  }

  private static Optional<String> functionName(Resource resource) {
    return MappingTreeUtils.getValue(resource.properties(), "FunctionName")
      .filter(ScalarTree.class::isInstance).map(s -> ((ScalarTree) s).value());
  }

  private static boolean isRelevantResource(Resource resource) {
    return RELEVANT_RESOURCE.contains(ScalarTreeUtils.getValue(resource.type()).orElse(null));
  }

  // Instinct functions can be nested in the LogGroupName property value and can extracted by a collecting TreeVisitor
  static class FunctionReferenceCollector extends TreeVisitor<TreeContext> {
    private static final Pattern SUB_PARAMETERS = Pattern.compile("\\$\\{([a-zA-Z0-9.]*)}");
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
        if (ScalarTreeUtils.isValue(tree.key(), "Fn::Sub")) {
          collectSubParameters(tree.value());
        } else if(ScalarTreeUtils.isValue(tree.key(), "Ref")) {
          collectRefParameter(tree.value());
        }
      });
    }

    private void collectSubParameters(CloudformationTree sub) {
      if (sub instanceof ScalarTree) {
        Matcher m = SUB_PARAMETERS.matcher(((ScalarTree) sub).value());
        while (m.find()) {
          references.add(m.group(1));
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
