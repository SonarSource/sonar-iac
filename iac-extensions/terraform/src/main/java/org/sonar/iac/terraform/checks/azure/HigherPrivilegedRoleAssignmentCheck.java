/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.checks.azure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;

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
    "fe930be7-5e62-47db-91af-98c3a49a38b1", "User Administrator"
  );

  private static final String MESSAGE = "Make sure that assigning the %s role is safe here.";

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> {
      HigherPrivilegedRoleCollector collector = HigherPrivilegedRoleCollector.collect(tree);
      checkAssignedRolePrivilegs(ctx, collector);
    });
  }

  private static void checkAssignedRolePrivilegs(CheckContext ctx, HigherPrivilegedRoleCollector collector) {
    collector.roleMember.stream()
      .filter(collector.higherPrivilegedRoles::containsKey)
      .map(collector.higherPrivilegedRoles::get)
      .forEach(role -> ctx.reportIssue(role, message(role.value())));
  }

  private static String message(String role) {
    return String.format(MESSAGE, HIGHER_PRIVILEGED_ROLE.getOrDefault(role, role));
  }

  private static class HigherPrivilegedRoleCollector extends TreeVisitor<TreeContext> {

    private final Map<String, TextTree> higherPrivilegedRoles = new HashMap<>();
    private final Set<String> roleMember = new HashSet<>();

    public HigherPrivilegedRoleCollector() {
      register(BlockTree.class, (ctx, blockTree) -> {
        if (isResource(blockTree, "azuread_directory_role") && hasReferenceLabel(blockTree)) {
          collectHigherPrivilegedRole(blockTree);
        } else if (isResource(blockTree, "azuread_directory_role_member")) {
          collectDirectoryRoleMember(blockTree);
        }
      });
    }

    private void collectHigherPrivilegedRole(BlockTree resource) {
      PropertyUtils.value(resource, "display_name")
        .filter(name -> TextUtils.matchesValue(name, HIGHER_PRIVILEGED_ROLE::containsValue).isTrue())
        .ifPresent(name -> higherPrivilegedRoles.putIfAbsent(getReferenceLabel(resource), (TextTree) name));

      PropertyUtils.value(resource, "template_id")
        .filter(name -> TextUtils.matchesValue(name, HIGHER_PRIVILEGED_ROLE::containsKey).isTrue())
        .ifPresent(id -> higherPrivilegedRoles.putIfAbsent(getReferenceLabel(resource), (TextTree) id));
    }


    private void collectDirectoryRoleMember(BlockTree resource) {
      PropertyUtils.value(resource, "role_object_id", AttributeAccessTree.class)
        .filter(HigherPrivilegedRoleCollector::isObjectIdReference)
        .map(HigherPrivilegedRoleCollector::getObjectReferenceLabel)
        .ifPresent(roleMember::add);
    }

    private static boolean isObjectIdReference(AttributeAccessTree tree) {
      return "object_id".equals(tree.attribute().value()) && tree.object() instanceof AttributeAccessTree;
    }


    private static String getObjectReferenceLabel(AttributeAccessTree tree) {
      return ((AttributeAccessTree) tree.object()).attribute().value();
    }

    public static HigherPrivilegedRoleCollector collect(FileTree tree) {
      HigherPrivilegedRoleCollector collector = new HigherPrivilegedRoleCollector();
      collector.scan(new TreeContext(), tree);
      return collector;
    }

    private static String getReferenceLabel(BlockTree resource) {
      return resource.labels().get(1).value();
    }

    private static boolean hasReferenceLabel(BlockTree resource) {
      return resource.labels().size() >= 2;
    }
  }
}
