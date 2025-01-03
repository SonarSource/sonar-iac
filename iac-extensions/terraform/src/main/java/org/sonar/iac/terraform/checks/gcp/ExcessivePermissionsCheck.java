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
package org.sonar.iac.terraform.checks.gcp;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.ListSymbol;

@Rule(key = "S6406")
public class ExcessivePermissionsCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "This role grants more than %d sensitive permissions. Make sure they are all required.";
  private static final String SECONDARY_MESSAGE = "Sensitive permission";
  public static final int DEFAULT = 5;

  private static final List<String> SENSITIVE_ACTION_PREFIXES = List.of("abort", "access", "add", "allocate", "analyze", "apply", "approve", "associate", "attach", "begin",
    "bind", "call", "cancel", "clear", "close", "compute", "connect", "create", "delete", "deploy", "destroy", "detach",
    "disable", "drop", "enable", "evict", "exec", "import", "install", "invoke", "listVulnerabilities", "manage",
    "migrate", "move", "mutate", "patch", "pause", "proxy", "publish", "purchase", "purge", "put", "reject", "remove",
    "reopen", "replace", "rerun", "reset", "resize", "restart", "restore", "resume", "rollback", "rotate", "run",
    "sample", "scan", "send", "set", "sign", "sourceCodeGet", "sourceCodeSet", "start", "stop", "suspend", "undelete",
    "undeploy", "update", "upload", "use", "validate", "write");

  private static final List<String> SENSITIVE_ACTION_ELEMENTS = List.of("login", "create", "delete", "set");

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  @Override
  protected void registerResourceConsumer() {
    register(Set.of("google_organization_iam_custom_role", "google_project_iam_custom_role"),
      resource -> {
        ListSymbol permissions = resource.list("permissions");
        List<ExpressionTree> sensitivePermissions = resource.list("permissions")
          .getItemIf(isSensitivePermission())
          .toList();

        if (sensitivePermissions.size() > max) {
          List<SecondaryLocation> secondaries = sensitivePermissions.stream()
            .map(p -> new SecondaryLocation(p, SECONDARY_MESSAGE)).toList();
          permissions.report(String.format(MESSAGE, max), secondaries);
        }
      });
  }

  private static Predicate<ExpressionTree> isSensitivePermission() {
    return expression -> TextUtils.getValue(expression)
      .map(String::toLowerCase)
      .map(ExcessivePermissionsCheck::getPermissionSuffix)
      .filter(suffix -> !(suffix.contains("readonly")))
      .filter(ExcessivePermissionsCheck::isSensitiveSuffix)
      .isPresent();
  }

  private static String getPermissionSuffix(String permission) {
    return permission.substring(permission.lastIndexOf(".") + 1);
  }

  private static boolean isSensitiveSuffix(String suffix) {
    return SENSITIVE_ACTION_PREFIXES.stream().anyMatch(suffix::startsWith)
      || SENSITIVE_ACTION_ELEMENTS.stream().anyMatch(suffix::contains);
  }
}
