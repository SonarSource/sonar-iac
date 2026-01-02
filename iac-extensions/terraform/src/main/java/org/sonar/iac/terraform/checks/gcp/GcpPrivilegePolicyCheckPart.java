/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks.gcp;

import java.util.List;
import java.util.Set;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.matchesPattern;

public class GcpPrivilegePolicyCheckPart extends AbstractNewResourceCheck {

  private static final String POLICY_MESSAGE = "Make sure it is safe to give all members full access.";
  private static final String MEMBER_MESSAGE = "Make sure it is safe to grant that member full access.";
  private static final String SECONDARY_MESSAGE = "The policy is used here.";

  private static final String SENSITIVE_ROLES = ".*(?:admin|developer|manager|owner|superuser)(?:\\.?v\\d+)?";

  private final PolicyReferenceCollector collector = new PolicyReferenceCollector(IAM_POLICY_RESOURCE_TYPES);

  private static final Set<String> IAM_POLICY_RESOURCE_TYPES = Set.of(
    "google_project_iam_policy",
    "google_organization_iam_policy",
    "google_service_account_iam_policy",
    "google_folder_iam_policy");

  @Override
  public void initialize(InitContext init) {
    super.initialize(init);
    init.register(FileTree.class, (ctx, tree) -> {
      collector.reset();
      collector.scan(new TreeContext(), tree);
    });
  }

  @Override
  protected void provideResource(CheckContext ctx, BlockTree blockTree) {
    super.provideResource(ctx, blockTree);
    if (isDataOfType(blockTree, "google_iam_policy")) {
      ResourceSymbol dataData = ResourceSymbol.fromPresent(ctx, blockTree);
      collector.checkPolicy(dataData, matchesPattern(SENSITIVE_ROLES), POLICY_MESSAGE, SECONDARY_MESSAGE);
    }
  }

  @Override
  protected void registerResourceConsumer() {
    register(List.of("google_project_iam_binding", "google_organization_iam_binding",
      "google_service_account_iam_binding", "google_folder_iam_binding"),
      resource -> checkRole(resource, POLICY_MESSAGE));

    register(List.of("google_project_iam_member", "google_organization_iam_member",
      "google_service_account_iam_member", "google_folder_iam_member"),
      resource -> checkRole(resource, MEMBER_MESSAGE));
  }

  private void checkRole(ResourceSymbol resource, String message) {
    resource.attribute("role")
      .reportIf(matchesPattern(SENSITIVE_ROLES), message);
  }
}
