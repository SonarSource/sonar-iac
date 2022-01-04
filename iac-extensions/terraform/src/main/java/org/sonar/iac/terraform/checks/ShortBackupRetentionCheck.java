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
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S6364")
public class ShortBackupRetentionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that defining a short backup retention duration is safe here.";
  private static final int DEFAULT = 7;

  @RuleProperty(
    key = "backup_retention_duration",
    defaultValue = "" + DEFAULT)
  int backupRetentionDuration = DEFAULT;

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    String type = getResourceType(resource);
    if (("aws_db_instance".equals(type) && PropertyUtils.isMissing(resource, "source_db_instance_identifier"))
      || "aws_rds_cluster".equals(type)) {
      checkBackupRetentionPeriod(ctx, resource, backupRetentionDuration);
    }
  }

  private static void checkBackupRetentionPeriod(CheckContext ctx, BlockTree resource, int minPeriod) {
    PropertyUtils.value(resource, "backup_retention_period").ifPresentOrElse(period ->
        TextUtils.getIntValue(period).filter(currentPeriod -> currentPeriod < minPeriod)
          .ifPresent(currentPeriod -> ctx.reportIssue(period, MESSAGE)),
      () -> reportResource(ctx, resource, MESSAGE));
  }
}
