/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.cloudformation.checks.AbstractResourceCheck.Resource;
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

  private final Map<String, Resource> resourceWithoutLogGroup = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> {
      List<Resource> resources = getFileResources(tree);
      resources.stream().filter(LogGroupDeclarationCheck::isRelevantResource)
        .forEach(resource -> resourceWithoutLogGroup.put(resource.name().value(), resource));

      resources.stream().filter(resource -> resource.isType("AWS::Logs::LogGroup"))
        .filter(resource -> resource.properties() instanceof MappingTree)
        .forEach(this::resolveReferences);

      resourceWithoutLogGroup.forEach((name, resource) -> ctx.reportIssue(resource.type(), MESSAGE));
      resourceWithoutLogGroup.clear();
    });
  }

  private void resolveReferences(Resource resource) {
    ReferenceCollector.getReferences((MappingTree) resource.properties()).forEach(resourceWithoutLogGroup::remove);
  }

  private static boolean isRelevantResource(Resource resource) {
    return RELEVANT_RESOURCE.contains(ScalarTreeUtils.getValue(resource.type()).orElse(null));
  }

  static class ReferenceCollector extends TreeVisitor<TreeContext> {
    private static final Pattern SUB_PARAMETERS = Pattern.compile("\\$\\{([a-zA-Z0-9.]*)}");
    private final Set<String> references = new HashSet<>();

    public ReferenceCollector() {
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

    public static Set<String> getReferences(MappingTree properties) {
      ReferenceCollector collector = new ReferenceCollector();
      collector.scan(new TreeContext(), properties);
      return collector.references;
    }
  }
}
