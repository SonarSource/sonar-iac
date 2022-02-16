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
package org.sonar.iac.terraform.checks.gcp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.api.tree.TerraformTree.Kind.ATTRIBUTE_ACCESS;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.getResourceType;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.matchesPattern;
import static org.sonar.iac.terraform.checks.utils.TerraformUtils.attributeAccessToString;

public class GcpPrivilegePolicyCheckPart extends AbstractNewResourceCheck {

  private static final String POLICY_MESSAGE = "Make sure it is safe to give all members full access.";
  private static final String MEMBER_MESSAGE = "Make sure it is safe to grant that member full access.";
  private static final String SECONDARY_MESSAGE = "The policy is used here.";

  private static final String SENSITIVE_ROLES = ".*(?:admin|manager|owner|superuser).*";

  private final Map<String, AttributeTree> policyReferences = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    super.initialize(init);
    init.register(FileTree.class, (ctx, tree) -> new PolicyReferenceCollector().scan(new TreeContext(), tree));
  }

  @Override
  protected void provideResource(CheckContext ctx, BlockTree blockTree) {
    super.provideResource(ctx, blockTree);
    if (isData(blockTree)) {
      ResourceSymbol data = ResourceSymbol.fromPresent(ctx, blockTree);
      if ("google_iam_policy".equals(data.type)) {
        checkPolicy(data);
      }
    }
  }

  private static boolean isData(BlockTree blockTree) {
    return "data".equals(blockTree.key().value());
  }

  private void checkPolicy(ResourceSymbol data) {
    AttributeTree reference = policyReferences.get(String.format("data.google_iam_policy.%s.policy_data", data.name));
    if (reference != null) {
      SecondaryLocation secondary = new SecondaryLocation(reference, SECONDARY_MESSAGE);
      data.blocks("binding").forEach(
        binding -> binding.attribute("role")
          .reportIf(matchesPattern(SENSITIVE_ROLES), POLICY_MESSAGE, secondary));
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

  private class PolicyReferenceCollector extends TreeVisitor<TreeContext> {

    private final Set<String> relevantResources = Set.of("google_project_iam_policy", "google_organization_iam_policy",
      "google_service_account_iam_policy", "google_folder_iam_policy");

    public PolicyReferenceCollector() {
      policyReferences.clear();
      register(BlockTree.class, (ctx, tree) -> {
        if (isResource(tree) && relevantResources.contains(getResourceType(tree))) {
          collectReference(tree);
        }
      });
    }

    private void collectReference(BlockTree tree) {
      PropertyUtils.get(tree, "policy_data", AttributeTree.class)
        .filter(policyData -> policyData.value().is(ATTRIBUTE_ACCESS))
        .ifPresent(policyData -> {
          String key = attributeAccessToString((AttributeAccessTree) policyData.value());
          policyReferences.put(key, policyData);
        });
    }
  }
}
