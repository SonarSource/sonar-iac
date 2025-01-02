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

import org.sonar.check.Rule;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.ListSymbol;

@Rule(key = "S6414")
public class AuditLogMemberExclusionCheck extends AbstractNewResourceCheck {

  @Override
  protected void registerResourceConsumer() {
    register("google_project_iam_audit_config",
      resource -> resource.blocks("audit_log_config")
        .forEach(block -> {
          ListSymbol list = block.list("exempted_members");
          if (!list.isEmpty() && !list.isByReference()) {
            list.report("Make sure excluding members activity from audit logs is safe here.");
          }
        }));
  }
}
