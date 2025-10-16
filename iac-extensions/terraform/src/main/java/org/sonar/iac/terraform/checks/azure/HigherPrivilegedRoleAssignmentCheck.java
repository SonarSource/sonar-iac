/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks.azure;

import java.util.HashMap;
import java.util.Map;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.getReferenceLabel;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.hasReferenceLabel;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;

@Rule(key = "S6375")
public class HigherPrivilegedRoleAssignmentCheck implements IacCheck {

  private static final Map<String, String> HIGHER_PRIVILEGED_ROLE = Map.of(
    "9b895d92-2cd3-44c7-9d02-a6ac2d5ea5c3", "Application Administrator",
    "c4e39bd9-1100-46d3-8c65-fb160da0071f", "Authentication Administrator",
    "158c047a-c907-4556-b7ef-446551a6b5f7", "Cloud Application Administrator",
    "62e90394-69f5-4237-9190-012177145e10", "Global Administrator",
    "dd7a751-b60b-444a-984c-02652fe8fa1c", "Groups Administrator",
    "729827e3-9c14-49f7-bb1b-9608f156bbb8", "Helpdesk Administrator",
    "966707d0-3269-4727-9be2-8c3a10f19b9d", "Password Administrator",
    "7be44c8a-adaf-4e2a-84d6-ab2649e08a13", "Privileged Authentication Administrator",
    "e8611ab8-c189-46e8-94e1-60213ab1f814", "Privileged Role Administrator",
    "fe930be7-5e62-47db-91af-98c3a49a38b1", "User Administrator");

  private static final String MESSAGE = "Make sure that assigning the %s role is safe here.";
  private static final String SECONDARY_MESSAGE = "Role assigned here.";

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> {
      HigherPrivilegedRoleCollector collector = HigherPrivilegedRoleCollector.collect(tree);
      checkAssignedRolePrivileges(ctx, collector);
    });
  }

  private static void checkAssignedRolePrivileges(CheckContext ctx, HigherPrivilegedRoleCollector collector) {
    collector.roleMember.entrySet().stream()
      .filter(member -> collector.higherPrivilegedRoles.containsKey(member.getKey()))
      .forEach(member -> {
        AttributeTree role = collector.higherPrivilegedRoles.get(member.getKey());
        SecondaryLocation secondary = new SecondaryLocation(member.getValue(), SECONDARY_MESSAGE);
        ctx.reportIssue(role, message(((TextTree) role.value()).value()), secondary);
      });
  }

  private static String message(String role) {
    return String.format(MESSAGE, HIGHER_PRIVILEGED_ROLE.getOrDefault(role, role));
  }

  private static class HigherPrivilegedRoleCollector extends TreeVisitor<TreeContext> {

    private final Map<String, AttributeTree> higherPrivilegedRoles = new HashMap<>();
    private final Map<String, AttributeTree> roleMember = new HashMap<>();

    public HigherPrivilegedRoleCollector() {
      register(BlockTree.class, (ctx, blockTree) -> {
        if (isResource(blockTree, "azuread_directory_role") && hasReferenceLabel(blockTree)) {
          collectHigherPrivilegedRole(blockTree);
        } else if (isResource(blockTree, "azuread_directory_role_assignment")) {
          collectDirectoryRoleMember(blockTree, "role_id");
        } else if (isResource(blockTree, "azuread_directory_role_member")) {
          // this is an older version of role assignment resource:
          // https://registry.terraform.io/providers/hashicorp/azuread/latest/docs/resources/directory_role_member
          collectDirectoryRoleMember(blockTree, "role_object_id");
        }
      });
    }

    private void collectHigherPrivilegedRole(BlockTree resource) {
      PropertyUtils.get(resource, "display_name", AttributeTree.class)
        .filter(attribute -> TextUtils.matchesValue(attribute.value(), HIGHER_PRIVILEGED_ROLE::containsValue).isTrue())
        .ifPresent(name -> higherPrivilegedRoles.putIfAbsent(getReferenceLabel(resource), name));

      PropertyUtils.get(resource, "template_id", AttributeTree.class)
        .filter(attribute -> TextUtils.matchesValue(attribute.value(), HIGHER_PRIVILEGED_ROLE::containsKey).isTrue())
        .ifPresent(id -> higherPrivilegedRoles.putIfAbsent(getReferenceLabel(resource), id));
    }

    private void collectDirectoryRoleMember(BlockTree resource, String key) {
      PropertyUtils.get(resource, key, AttributeTree.class)
        .filter(attribute -> attribute.value().is(TerraformTree.Kind.ATTRIBUTE_ACCESS))
        // The old API accepts only `object_id`, but the new one also accepts `template_id`
        .filter(attribute -> isObjectIdReference((AttributeAccessTree) attribute.value()) || isTemplateIdReference((AttributeAccessTree) attribute.value()))
        .ifPresent(attribute -> roleMember.putIfAbsent(getObjectReferenceLabel((AttributeAccessTree) attribute.value()), attribute));
    }

    private static boolean isObjectIdReference(AttributeAccessTree tree) {
      return "object_id".equals(tree.attribute().value()) && tree.object() instanceof AttributeAccessTree;
    }

    private static boolean isTemplateIdReference(AttributeAccessTree tree) {
      return "template_id".equals(tree.attribute().value()) && tree.object() instanceof AttributeAccessTree;
    }

    private static String getObjectReferenceLabel(AttributeAccessTree tree) {
      return ((AttributeAccessTree) tree.object()).attribute().value();
    }

    public static HigherPrivilegedRoleCollector collect(FileTree tree) {
      HigherPrivilegedRoleCollector collector = new HigherPrivilegedRoleCollector();
      collector.scan(new TreeContext(), tree);
      return collector;
    }
  }
}
