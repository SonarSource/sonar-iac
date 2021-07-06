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
        .forEach(this::resolveRelation);

      resourceWithoutLogGroup.forEach((name, resource) -> ctx.reportIssue(resource.type(), MESSAGE));
      resourceWithoutLogGroup.clear();
    });
  }

  private void resolveRelation(Resource resource) {
    RelationCollector collector = RelationCollector.collect((MappingTree) resource.properties());
    collector.subParameter.forEach(resourceWithoutLogGroup::remove);
  }

  private static boolean isRelevantResource(Resource resource) {
    return RELEVANT_RESOURCE.contains(ScalarTreeUtils.getValue(resource.type()).orElse(null));
  }

  static class RelationCollector extends TreeVisitor<TreeContext> {
    private static final Pattern SUB_PARAMETERS = Pattern.compile("\\$\\{([a-zA-Z0-9.]*)}");
    protected final Set<String> subParameter = new HashSet<>();

    public RelationCollector() {
      register(ScalarTree.class, (ctx, tree) -> {
        if ("!Sub".equals(tree.tag())) {
          collectSubParameter(tree);
        }
      });
      register(TupleTree.class, (ctx, tree) -> {
        if (ScalarTreeUtils.isValue(tree.key(), "Fn::Sub")) {
          collectSubParameter(tree.value());
        }
      });
    }

    private void collectSubParameter(CloudformationTree sub) {
      if (sub instanceof ScalarTree) {
        Matcher m = SUB_PARAMETERS.matcher(((ScalarTree) sub).value());
        while (m.find()) {
          subParameter.add(m.group(1));
        }
      }
    }

    public static RelationCollector collect(MappingTree properties) {
      RelationCollector collector = new RelationCollector();
      collector.scan(new TreeContext(), properties);
      return collector;
    }
  }
}
