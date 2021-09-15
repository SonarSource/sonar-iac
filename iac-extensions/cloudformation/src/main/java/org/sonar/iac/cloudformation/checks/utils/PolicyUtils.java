/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

public class PolicyUtils {

  private PolicyUtils() {
  }

  public static List<Policy> getPolicies(CloudformationTree root) {
    PolicyCollector collector = new PolicyCollector();
    collector.scan(new TreeContext(), root);
    return collector.policies;
  }

  public static class Policy {
    private final CloudformationTree version;
    private final CloudformationTree id;
    private final List<Statement> statement;

    public Policy(CloudformationTree policyDocument) {
      this.version = PropertyUtils.valueOrNull(policyDocument, "Version", CloudformationTree.class);
      this.id = PropertyUtils.valueOrNull(policyDocument, "Id", CloudformationTree.class);
      this.statement = XPathUtils.getTrees(policyDocument, "/Statement[]").stream().map(Statement::new).collect(Collectors.toList());
    }

    public Optional<CloudformationTree> version() {
      return Optional.ofNullable(version);
    }

    public Optional<CloudformationTree> id() {
      return Optional.ofNullable(id);
    }

    public List<Statement> statement() {
      return statement;
    }
  }

  public static class Statement {
    private final CloudformationTree sid;
    private final CloudformationTree effect;
    private final CloudformationTree principal;
    private final CloudformationTree notPrincipal;
    private final CloudformationTree action;
    private final CloudformationTree notAction;
    private final CloudformationTree resource;
    private final CloudformationTree notResource;
    private final CloudformationTree condition;

    public Statement(CloudformationTree statement) {
      this.sid = PropertyUtils.valueOrNull(statement, "Sid", CloudformationTree.class);
      this.effect = PropertyUtils.valueOrNull(statement, "Effect", CloudformationTree.class);
      this.principal = PropertyUtils.valueOrNull(statement, "Principal", CloudformationTree.class);
      this.notPrincipal = PropertyUtils.valueOrNull(statement, "NotPrincipal", CloudformationTree.class);
      this.action = PropertyUtils.valueOrNull(statement, "Action", CloudformationTree.class);
      this.notAction = PropertyUtils.valueOrNull(statement, "NotAction", CloudformationTree.class);
      this.resource = PropertyUtils.valueOrNull(statement, "Resource", CloudformationTree.class);
      this.notResource = PropertyUtils.valueOrNull(statement, "NotResource", CloudformationTree.class);
      this.condition = PropertyUtils.valueOrNull(statement, "Condition", CloudformationTree.class);
    }

    public Optional<CloudformationTree> sid() {
      return Optional.ofNullable(sid);
    }

    public Optional<CloudformationTree> effect() {
      return Optional.ofNullable(effect);
    }

    public Optional<CloudformationTree> principal() {
      return Optional.ofNullable(principal);
    }

    public Optional<CloudformationTree> notPrincipal() {
      return Optional.ofNullable(notPrincipal);
    }

    public Optional<CloudformationTree> action() {
      return Optional.ofNullable(action);
    }

    public Optional<CloudformationTree> notAction() {
      return Optional.ofNullable(notAction);
    }

    public Optional<CloudformationTree> resource() {
      return Optional.ofNullable(resource);
    }

    public Optional<CloudformationTree> notResource() {
      return Optional.ofNullable(notResource);
    }

    public Optional<CloudformationTree> condition() {
      return Optional.ofNullable(condition);
    }
  }

  private static class PolicyCollector extends TreeVisitor<TreeContext> {
    private final List<Policy> policies = new ArrayList<>();

    public PolicyCollector() {
      register(TupleTree.class, (ctx, tree) -> collectPolicy(tree));
    }

    private void collectPolicy(TupleTree tree) {
      if (isPolicyDocument(tree)) {
        policies.add(new Policy(tree.value()));
      }
    }

    private static boolean isPolicyDocument(TupleTree tree) {
      return TextUtils.getValue(tree.key()).filter(v -> v.toLowerCase(Locale.ROOT).contains("policy")).isPresent()
          && !XPathUtils.getTrees(tree.value(), "/Statement[]").isEmpty();
    }
  }
}
